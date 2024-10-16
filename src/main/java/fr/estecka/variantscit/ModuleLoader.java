package fr.estecka.variantscit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import com.google.gson.JsonObject;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import fr.estecka.variantscit.api.ICitModule;
import net.fabricmc.fabric.api.client.model.loading.v1.PreparableModelLoadingPlugin.DataLoader;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public final class ModuleLoader
implements DataLoader<ModuleLoader.Result>
{
	static public class Result {
		public final HashMap<Identifier, BakedModule> uniqueModules = new HashMap<>();
		public final HashSet<Identifier> ignoredModules = new HashSet<>();
		public final Map<RegistryEntry<Item>,List<MetaModule>> modulesPerItem = new HashMap<>();
		public final ModelAggregator modelAggregator = new ModelAggregator();
	}

	static record ProtoModule (
		ModuleDefinition definition,
		JsonObject parameters
	){}

	/**
	 * Contains all the data  pertaining to  a module file. Most of this data is
	 * only  relevant  to  the  resource-loading  phase, and  will be  discarded
	 * afterward.
	 */
	static public record MetaModule (
		Identifier id,
		ProtoModule prototype,
		BakedModule bakedModule
	){}

	@Override
	public CompletableFuture<ModuleLoader.Result> load(ResourceManager resourceManager, Executor executor){
		return CompletableFuture.supplyAsync(()->ReloadModules(resourceManager), executor);
	}

	static private ModuleLoader.Result ReloadModules(ResourceManager manager)
	{
		ModuleLoader.Result result = new ModuleLoader.Result();

		Map<Identifier, Resource> resources = new HashMap<>();
		resources.putAll(manager.findResources("variant-cits/item", id->id.getPath().endsWith(".json")));
		Warn(resources);
		resources.putAll(manager.findResources("variants-cit/item", id->id.getPath().endsWith(".json")));

		for (var entry : resources.entrySet())
		try {
			Identifier moduleId = ModuleIdFromResourceId(entry.getKey());
			ProtoModule prototype = DefinitionFromResource(entry.getValue()).getOrThrow();

			Set<RegistryEntry<Item>> targets = prototype.definition.targets()
				.map(ModuleLoader::ItemsFromTarget)
				.orElseGet(()->ItemsFromModuleId(moduleId))
				;
			if (targets.isEmpty()){
				result.ignoredModules.add(moduleId);
				continue;
			}

			ICitModule moduleLogic = ModuleRegistry.CreateModule(prototype.definition, prototype.parameters);
			VariantLibrary library = result.modelAggregator.CreateLibrary(prototype, manager);
			MetaModule meta = new MetaModule(
				moduleId,
				prototype,
				new BakedModule(library, moduleLogic)
			);

			result.uniqueModules.put(moduleId, meta.bakedModule());
			for (var item : targets){
				result.modulesPerItem.computeIfAbsent(item, __->new ArrayList<>()).add(meta);
			}
		}
		catch (IllegalStateException e){
			VariantsCitMod.LOGGER.error("Error in CIT module {}: {}", entry.getKey(), e);
		}

		// Sort highest priorities first.
		for (List<MetaModule> modules : result.modulesPerItem.values()){
			modules.sort((a,b) -> -Integer.compare(a.prototype.definition.priority(), b.prototype.definition.priority()));
		}
		return result;
	}

	static private void Warn(Map<Identifier, Resource> resources){
		if (!resources.isEmpty()){
			String names = "";
			for (Identifier id : resources.keySet()) {
				names += ' ';
				names += ModuleIdFromResourceId(id).toString();
			}
			VariantsCitMod.LOGGER.warn("Some CIT modules are using the old mispelled directory `variant-cits`, those should be moved to `variants-cit` instead:{}", names);
		}
	}

	static private Set<RegistryEntry<Item>> ItemsFromTarget(List<Identifier> targets){
		Set<RegistryEntry<Item>> result = new HashSet<>();
		targets.stream()
			.map(id->Registries.ITEM.getEntry(id).get())
			.filter(o->o!=null)
			.forEach(result::add)
			;
		return result;
	}

	static private Set<RegistryEntry<Item>> ItemsFromModuleId(Identifier moduleId){
		Identifier itemId = ItemIdFromModuleId(moduleId);

		if (Registries.ITEM.containsId(itemId))
			return Set.of(Registries.ITEM.getEntry(itemId).get());
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
		catch (IOException e){
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
}
