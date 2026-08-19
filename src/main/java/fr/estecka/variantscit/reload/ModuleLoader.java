package fr.estecka.variantscit.reload;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.item.Item;
import fr.estecka.variantscit.modules.IBakedModule;
import fr.estecka.variantscit.modules.PreconditionModule;
import fr.estecka.variantscit.CodecUtil;
import fr.estecka.variantscit.VariantsCitMod;
import fr.estecka.variantscit.assetgen.HotswappableResourceManager;
import fr.estecka.variantscit.collections.HashMap3;


public final class ModuleLoader
{
	static public class Result {
		/** Sorted by hook, target, and priority. */
		public final HashMap3<EModuleHook,Item,Integer,List<IBakedModule>> sortedModules = new HashMap3<>();

		public final Map<Identifier, MetaModule> uniqueModules = new HashMap<>();
		public final Map<Identifier, String> moduleErrors;
		public final VariantAggregator variantAggregator;

		private Result(
			Map<Identifier,ModuleDefinition> modules,
			Map<Identifier,String> errors
		){
			this.variantAggregator = new VariantAggregator(modules);
			this.moduleErrors = errors;
		}
	}

	static public ModuleLoader.Result ReloadModules(HotswappableResourceManager manager)
	{
		final ModuleLoader.Result result;
		final Map<Identifier, String> errors = new HashMap<>();
		final List<MetaModule> metamodules = new ArrayList<>();

		Map<Identifier, Resource> resources = new HashMap<>();
		Map<Identifier, ModuleDefinition> definitions = new HashMap<>();
		resources.putAll(CodecUtil.GetResources(manager.Get(), "variant-cits/item", ".json"));
		ObsoletePathWarning(resources);
		resources.putAll(CodecUtil.GetResources(manager.Get(), "variants-cit/item", ".json"));
		resources.putAll(CodecUtil.GetResources(manager.Get(), "variants-cit/modules", ".json"));
		for (var entry : resources.entrySet())
		{
			Identifier moduleId = entry.getKey();
			var optDefinition = CodecUtil.ParseResource(entry.getValue(), ModuleDefinition.CODEC);
			if (optDefinition.isError()){
				String errormMsg = optDefinition.error().get().message();
				VariantsCitMod.LOGGER.error("Error in VCIT module {}: {}", moduleId, errormMsg);
				errors.put(moduleId, errormMsg);
				continue;
			}

			ModuleDefinition definition = optDefinition.getOrThrow();
			if (definition.hooks().isEmpty()){
				String errorMsg = "Skipped VCIT module with no hook: " + moduleId;
				VariantsCitMod.LOGGER.warn("{}", errorMsg);
				errors.put(moduleId, errorMsg);
				continue;
			}
			if (ItemsFromModule(moduleId, definition).isEmpty()){
				String errorMsg = "Skipped VCIT module with no valid item: " + moduleId;
				VariantsCitMod.LOGGER.warn("{}", errorMsg);
				errors.put(moduleId, errorMsg);
				continue;
			}

			definitions.put(moduleId, definition);
		}

		result = new Result(definitions, errors);
		result.variantAggregator.GatherAll(manager);

		for (var entry : definitions.entrySet())
		{
			Identifier moduleId = entry.getKey();
			ModuleDefinition definition = entry.getValue();
			Set<Item> targets = ItemsFromModule(moduleId, definition);
			var baked = EModuleHook.MapOf(
				hook -> result.variantAggregator.GetLibrary(hook, definition)
					.map(definition.parameters()::Bake)
					.map(module -> definition.precondition().isPresent() ? new PreconditionModule(definition.precondition().get(), module) : module)
					.orElse(null)
			);
			var libraries = EModuleHook.MapOf(
				hook -> result.variantAggregator.GetLibrary(hook, definition)
					.orElse(null)
			);

			MetaModule meta = new MetaModule(
				moduleId,
				definition.priority(),
				targets,
				definition.parameters(),
				definition.libraryDefinition(),
				libraries,
				baked
			);

			result.uniqueModules.put(moduleId, meta);
			metamodules.add(meta);
		}

		if (!result.variantAggregator.conflictingModelPrefixes.isEmpty()){
			String message = "Some modules with identical model prefixes have conflicting model parents, "
			               + "it is undefined which parent will be used. "
			               + "The following prefixes are involved: "
			               ;
			for (var prefix : result.variantAggregator.conflictingModelPrefixes)
				message += "\n - " + prefix;
			VariantsCitMod.LOGGER.error(message);
		}

		for (MetaModule meta : metamodules)
		for (Item item : meta.targets())
		for (var baked : meta.bakedModules().entrySet())
		{
			result.sortedModules.computeIfAbsent(baked.getKey(), item, (Integer)meta.priority(), ArrayList::new)
				.add(baked.getValue())
				;
		}

		return result;
	}

	static private void ObsoletePathWarning(Map<Identifier, Resource> resources){
		if (!resources.isEmpty()){
			String names = "";
			for (Identifier id : resources.keySet()) {
				names += ' ';
				names += id.toString();
			}
			VariantsCitMod.LOGGER.warn("Some VCIT modules are using the old mispelled directory `variant-cits`, those should be moved to `variants-cit` instead:{}", names);
		}
	}


/******************************************************************************/
/* # Target Item Baking                                                       */
/******************************************************************************/

	static private Set<Item> ItemsFromModule(Identifier moduleId, ModuleDefinition module){
		return module.targets()
			.map(ModuleLoader::ItemsFromTarget)
			.orElseGet(()->ItemsFromModuleId(moduleId))
			;
	}

	static private Set<Item> ItemsFromTarget(List<Identifier> targets){
		Set<Item> result = new HashSet<>();
		targets.stream()
			.map(id->BuiltInRegistries.ITEM.get(id))
			.filter(Optional::isPresent)
			.map(opt->opt.get().value())
			.forEach(result::add)
			;
		return result;
	}

	static private Set<Item> ItemsFromModuleId(Identifier moduleId){
		if (BuiltInRegistries.ITEM.containsKey(moduleId))
			return Set.of(BuiltInRegistries.ITEM.get(moduleId).get().value());
		else
			return Set.of();
	}
}
