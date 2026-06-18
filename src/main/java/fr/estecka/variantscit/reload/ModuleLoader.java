package fr.estecka.variantscit.reload;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.item.Item;
import fr.estecka.variantscit.modules.IBakedModule;
import fr.estecka.variantscit.modules.PreconditionModule;
import fr.estecka.variantscit.CodecUtil;
import fr.estecka.variantscit.VariantsCitMod;
import fr.estecka.variantscit.assetgen.HotswappableResourceManager;
import fr.estecka.variantscit.collections.TriMap;


public final class ModuleLoader
{
	static public class Result {
		/** Sorted by hook, target, and priority. */
		public final TriMap<EModuleHook,Item,Integer,List<IBakedModule>> sortedModules = new TriMap<>();

		public final Map<ResourceLocation, MetaModule> uniqueModules = new HashMap<>();
		public final VariantAggregator variantAggregator;

		private Result(Map<ResourceLocation,ModuleDefinition> modules){
			this.variantAggregator = new VariantAggregator(modules);
		}
	}

	static public ModuleLoader.Result ReloadModules(HotswappableResourceManager manager)
	{
		final ModuleLoader.Result result;
		final List<MetaModule> metamodules = new ArrayList<>();

		Map<ResourceLocation, Resource> resources = new HashMap<>();
		Map<ResourceLocation, ModuleDefinition> definitions = new HashMap<>();
		resources.putAll(CodecUtil.GetResources(manager.Get(), "variant-cits/item", ".json"));
		ObsoletePathWarning(resources);
		resources.putAll(CodecUtil.GetResources(manager.Get(), "variants-cit/item", ".json"));
		resources.putAll(CodecUtil.GetResources(manager.Get(), "variants-cit/modules", ".json"));
		for (var entry : resources.entrySet())
		{
			ResourceLocation moduleId = entry.getKey();
			var optDefinition = CodecUtil.ParseResource(entry.getValue(), ModuleDefinition.CODEC);
			if (optDefinition.isError()){
				VariantsCitMod.LOGGER.error("Error in VCIT module {}: {}", moduleId, optDefinition.error().get().message());
				continue;
			}

			ModuleDefinition definition = optDefinition.getOrThrow();
			if (definition.hooks().isEmpty()){
				VariantsCitMod.LOGGER.warn("Skipped VCIT module with no hook: {}", moduleId);
				continue;
			}
			if (ItemsFromModule(moduleId, definition).isEmpty()){
				VariantsCitMod.LOGGER.warn("Skipped VCIT module with no valid item: {}", moduleId);
				continue;
			}

			definitions.put(moduleId, definition);
		}

		result = new Result(definitions);
		result.variantAggregator.GatherAll(manager);

		for (var entry : definitions.entrySet())
		{
			ResourceLocation moduleId = entry.getKey();
			ModuleDefinition definition = entry.getValue();
			Set<Item> targets = ItemsFromModule(moduleId, definition);
			var baked = EModuleHook.MapOf(
				ctx -> result.variantAggregator.GetLibrary(ctx, definition)
					.map(definition.parameters()::Bake)
					.map(module -> definition.precondition().isPresent() ? new PreconditionModule(definition.precondition().get(), module) : module)
					.orElse(null)
			);

			MetaModule meta = new MetaModule(
				moduleId,
				definition.priority(),
				targets,
				definition.libraryDefinition(),
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

	static private void ObsoletePathWarning(Map<ResourceLocation, Resource> resources){
		if (!resources.isEmpty()){
			String names = "";
			for (ResourceLocation id : resources.keySet()) {
				names += ' ';
				names += id.toString();
			}
			VariantsCitMod.LOGGER.warn("Some VCIT modules are using the old mispelled directory `variant-cits`, those should be moved to `variants-cit` instead:{}", names);
		}
	}


/******************************************************************************/
/* # Target Item Baking                                                       */
/******************************************************************************/

	static private Set<Item> ItemsFromModule(ResourceLocation moduleId, ModuleDefinition module){
		return module.targets()
			.map(ModuleLoader::ItemsFromTarget)
			.orElseGet(()->ItemsFromModuleId(moduleId))
			;
	}

	static private Set<Item> ItemsFromTarget(List<ResourceLocation> targets){
		Set<Item> result = new HashSet<>();
		targets.stream()
			.map(id->BuiltInRegistries.ITEM.get(id))
			.filter(Optional::isPresent)
			.map(opt->opt.get().value())
			.forEach(result::add)
			;
		return result;
	}

	static private Set<Item> ItemsFromModuleId(ResourceLocation moduleId){
		if (BuiltInRegistries.ITEM.containsKey(moduleId))
			return Set.of(BuiltInRegistries.ITEM.get(moduleId).get().value());
		else
			return Set.of();
	}
}
