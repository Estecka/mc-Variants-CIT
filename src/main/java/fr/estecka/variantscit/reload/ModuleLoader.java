package fr.estecka.variantscit.reload;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import fr.estecka.variantscit.modules.IBakedModule;
import fr.estecka.variantscit.CodecUtil;
import fr.estecka.variantscit.VariantsCitMod;
import fr.estecka.variantscit.assetgen.HotswappableResourceManager;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;


public final class ModuleLoader
{
	static public class Result {
		public final Map<Item, IBakedModule> itemModules  = new HashMap<>();
		public final Map<Item, IBakedModule> equipModules = new HashMap<>();
		public final Map<Identifier, MetaModule> allModules = new HashMap<>();
		public final VariantAggregator variantAggregator;

		private Result(Map<Identifier,ModuleDefinition> modules){
			this.variantAggregator = new VariantAggregator(modules);
		}
	}

	static public ModuleLoader.Result ReloadModules(HotswappableResourceManager manager)
	{
		final ModuleLoader.Result result;
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
				VariantsCitMod.LOGGER.error("Error in VCIT module {}: {}", moduleId, optDefinition.error().get().message());
				continue;
			}

			ModuleDefinition definition = optDefinition.getOrThrow();
			if (definition.contexts().isEmpty()){
				VariantsCitMod.LOGGER.warn("Skipped VCIT module with no context: {}", moduleId);
				continue;
			}
			if (ItemsFromModule(moduleId, definition).isEmpty()){
				VariantsCitMod.LOGGER.warn("Skipped VCIT module with no valid item: {}", moduleId);
				continue;
			}
			if (definition.modelPrefix().isEmpty())
				VariantsCitMod.LOGGER.error("VCIT module `{}` has an empty model prefix. This can lead to unexpected behaviours and performance loss.", moduleId);

			definitions.put(moduleId, definition);
		}

		result = new Result(definitions);
		result.variantAggregator.GatherAll(manager);

		for (var entry : definitions.entrySet())
		{
			Identifier moduleId = entry.getKey();
			ModuleDefinition definition = entry.getValue();
			Set<Item> targets = ItemsFromModule(moduleId, definition);
			MetaModule meta = new MetaModule(
				moduleId,
				definition.priority(),
				targets,
				definition.modelPrefix(),
				result.variantAggregator.GetLibrary(EModuleContext.ITEM_MODEL, definition).map(definition.parameters()::Bake),
				result.variantAggregator.GetLibrary(EModuleContext.EQUIPPABLE, definition).map(definition.parameters()::Bake)
			);

			result.allModules.put(moduleId, meta);
			metamodules.add(meta);
		}

		// Sort highest priorities first.
		metamodules.sort((a,b) -> -Integer.compare(a.priority(), b.priority()));

		if (!result.variantAggregator.conflictingModelPrefixes.isEmpty()){
			String message = "Some modules with identical model prefixes have conflicting model parents, "
			               + "it is undefined which parent will be used. "
			               + "The following prefixes are involved: "
			               ;
			for (var prefix : result.variantAggregator.conflictingModelPrefixes)
				message += "\n - " + prefix;
			VariantsCitMod.LOGGER.error(message);
		}

		BakeModules(result, metamodules);
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
			.map(id->Registries.ITEM.getEntry(id))
			.filter(Optional::isPresent)
			.map(opt->opt.get().value())
			.forEach(result::add)
			;
		return result;
	}

	static private Set<Item> ItemsFromModuleId(Identifier moduleId){
		if (Registries.ITEM.containsId(moduleId))
			return Set.of(Registries.ITEM.getEntry(moduleId).get().value());
		else
			return Set.of();
	}


/******************************************************************************/
/* # Module Baking                                                            */
/******************************************************************************/

	static public void BakeModules(ModuleLoader.Result result, List<MetaModule> modules){
		Map<Item, List<IBakedModule>> itemModules  = new HashMap<>();
		Map<Item, List<IBakedModule>> equipModules = new HashMap<>();
	
		for (MetaModule meta : modules)
		{
			if (meta.itemModule ().isPresent()) BakeModuleContext(meta, meta.itemModule ().get(), itemModules );
			if (meta.equipModule().isPresent()) BakeModuleContext(meta, meta.equipModule().get(), equipModules);
		}

		BakeItem(result.itemModules,  itemModules );
		BakeItem(result.equipModules, equipModules);
	}

	static private void BakeModuleContext(MetaModule meta, IBakedModule bakedModule, Map<Item, List<IBakedModule>> output){
		for (Item itemType : meta.targets()){
			output.computeIfAbsent(itemType, __->new ArrayList<>()).add(bakedModule);
		}
	}

	static private void BakeItem(Map<Item, IBakedModule> result, Map<Item, List<IBakedModule>> moduleListPerItem){
		for (var entry : moduleListPerItem.entrySet()){
			result.put(
				entry.getKey(),
				IBakedModule.OfList( entry.getValue() )
			);
		}
	}


}
