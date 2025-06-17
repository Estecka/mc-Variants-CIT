package fr.estecka.variantscit.reload;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import fr.estecka.variantscit.BakedModule;
import fr.estecka.variantscit.IItemModelProvider;
import fr.estecka.variantscit.ModuleRegistry;
import fr.estecka.variantscit.VariantLibrary;
import fr.estecka.variantscit.VariantsCitMod;
import fr.estecka.variantscit.api.ICitModule;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public final class ModuleLoader
{
	static public class Result {
		public final Map<Item, IItemModelProvider> itemModules  = new HashMap<>();
		public final Map<Item, IItemModelProvider> equipModules = new HashMap<>();
		public final ItemVariantAggregator  itemAggregator  = new ItemVariantAggregator ();
		public final EquipVariantAggregator equipAggregator = new EquipVariantAggregator();
	}

	static record ProtoModule (
		ModuleDefinition definition,
		JsonObject parameters
	){}

	/**
	 * Contains all the processed data about a module. Some of that data is only
	 * relevant to the resource-loading phase, and will be discarded at the end.
	 */
	static public record MetaModule (
		Identifier id,
		int priority,
		Set<Item> targets,
		Optional<VariantLibrary> itemLibrary,
		Optional<VariantLibrary> equipLibrary,
		ICitModule logic
		
	){}

	static public ModuleLoader.Result ReloadModules(ResourceManager manager)
	{
		final ModuleLoader.Result result = new ModuleLoader.Result();
		final List<MetaModule> modules = new ArrayList<>();

		Map<Identifier, Resource> resources = new HashMap<>();
		resources.putAll(manager.findResources("variant-cits/item", id->id.getPath().endsWith(".json")));
		ObsoletePathWarning(resources);
		resources.putAll(manager.findResources("variants-cit/item", id->id.getPath().endsWith(".json")));
		// resources.putAll(manager.findResources("variants-cit/equipment", id->id.getPath().endsWith(".json")));

		for (var entry : resources.entrySet())
		try {
			Identifier moduleId = ModuleIdFromResourceId(entry.getKey());
			ProtoModule prototype = DefinitionFromResource(entry.getValue()).getOrThrow();

			List<EModuleFeature> enabledFeatures = prototype.definition().GetEnabledFeatures(moduleId);
			if (enabledFeatures.isEmpty()){
				VariantsCitMod.LOGGER.warn("Ignored CIT module with no feature: {}", moduleId);
				continue;
			}

			Set<Item> targets = prototype.definition.targets()
				.map(ModuleLoader::ItemsFromTarget)
				.orElseGet(()->ItemsFromModuleId(moduleId))
				;
			if (targets.isEmpty()){
				VariantsCitMod.LOGGER.warn("Skipped VCIT module with no valid item: {}", moduleId);
				continue;
			}

			if (prototype.definition.modelPrefix().isEmpty())
				VariantsCitMod.LOGGER.error("VCIT module `{}` has an empty model prefix. This can lead to unexpected behaviours and performance loss.", moduleId);

			ICitModule moduleLogic = ModuleRegistry.CreateModule(prototype.definition.type(), prototype.parameters);
			VariantLibrary itemLibrary = null;
			VariantLibrary equipLibrary = null;

			for (EModuleFeature f : enabledFeatures)
			switch (f) {
				case ITEM_MODEL: itemLibrary  = result.itemAggregator .CreateLibrary(prototype.definition, manager); break;
				case EQUIPPABLE: equipLibrary = result.equipAggregator.CreateLibrary(prototype.definition, manager); break;
			}

			MetaModule meta = new MetaModule(
				moduleId,
				prototype.definition.priority(),
				targets,
				Optional.ofNullable(itemLibrary),
				Optional.ofNullable(equipLibrary),
				moduleLogic
			);

			modules.add(meta);
		}
		catch (IllegalStateException e){
			VariantsCitMod.LOGGER.error("Error in VCIT module {}: {}", entry.getKey(), e);
		}

		// Sort highest priorities first.
		modules.sort((a,b) -> -Integer.compare(a.priority(), b.priority()));

		BakeModules(result, modules);
		return result;
	}

	static private void ObsoletePathWarning(Map<Identifier, Resource> resources){
		if (!resources.isEmpty()){
			String names = "";
			for (Identifier id : resources.keySet()) {
				names += ' ';
				names += ModuleIdFromResourceId(id).toString();
			}
			VariantsCitMod.LOGGER.warn("Some VCIT modules are using the old mispelled directory `variant-cits`, those should be moved to `variants-cit` instead:{}", names);
		}
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
		Identifier itemId = ItemIdFromModuleId(moduleId);

		if (Registries.ITEM.containsId(itemId))
			return Set.of(Registries.ITEM.getEntry(itemId).get().value());
		else
			return Set.of();
	}

	static private Identifier ItemIdFromModuleId(Identifier resource){
		String path = resource.getPath();
		path = path.substring("item/".length());
		return Identifier.of(resource.getNamespace(), path);
	}

	static private Identifier ModuleIdFromResourceId(Identifier resource){
		String path = resource.getPath();
		path = path.substring("variant-cits/".length(), path.length()-".json".length());
		return Identifier.of(resource.getNamespace(), path);
	}

	static private DataResult<ProtoModule> DefinitionFromResource(Resource resource){
		JsonObject json;
		try {
			json = JsonHelper.deserialize(resource.getReader());
		}
		catch (IOException|JsonParseException e){
			return DataResult.error(e::toString);
		}

		var dataResult = ModuleDefinition.CODEC.decoder().decode(JsonOps.INSTANCE, json);
		if (dataResult.isError()){
			return DataResult.error(dataResult.error().get()::message);
		}

		try {
			ModuleDefinition definition = dataResult.getOrThrow().getFirst();
			JsonObject parameters = json.getAsJsonObject("parameters");
			if (parameters == null)
				parameters = new JsonObject();

			return DataResult.success(new ProtoModule(definition, parameters));
		}
		catch (IllegalStateException|ClassCastException e){
			return DataResult.error(e::toString);
		}
	}

	static public void BakeModules(ModuleLoader.Result result, List<MetaModule> modules){
		Map<Item, List<BakedModule>> itemModules  = new HashMap<>();
		Map<Item, List<BakedModule>> equipModules = new HashMap<>();
	
		for (MetaModule meta : modules)
		{
			if (meta.itemLibrary() .isPresent()) BakeModuleFeature("item_model", meta, meta.itemLibrary ().get(), itemModules );
			if (meta.equipLibrary().isPresent()) BakeModuleFeature("equippable", meta, meta.equipLibrary().get(), equipModules);
		}

		BakeItem(result.itemModules,  itemModules );
		BakeItem(result.equipModules, equipModules);
	}

	static private void BakeModuleFeature(String featureName, MetaModule meta, VariantLibrary lib, Map<Item, List<BakedModule>> output){
		if (lib.isEmpty())
			VariantsCitMod.LOGGER.warn("Empty {} VCIT module {}", featureName, meta.id());
		else
			VariantsCitMod.LOGGER.info("Found {} {} variants for VCIT module {}", lib.GetVariantCount(), featureName, meta.id());

		for (Item itemType : meta.targets()){
			output.computeIfAbsent(itemType, __->new ArrayList<>()).add(new BakedModule(lib, meta.logic()));
		}
	}

	static private void BakeItem(Map<Item, IItemModelProvider> result, Map<Item, List<BakedModule>> moduleListPerItem){
		for (var entry : moduleListPerItem.entrySet()){
			result.put(
				entry.getKey(),
				IItemModelProvider.OfList( entry.getValue() )
			);
		}
	}


}
