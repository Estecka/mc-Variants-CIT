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
import com.ibm.icu.impl.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
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
		public final Map<RegistryEntry<Item>,List<MetaModule>> modulesPerItem = new HashMap<>();
	}

	/**
	 * Contains all the data  pertaining to  a module file. Most of this data is
	 * only  relevant  to  the  resource-loading  phase, and  will be  discarded
	 * afterward.
	 */
	static public class MetaModule {
		public final Identifier id;
		public final VariantManager manager;
		public final ModuleDefinition definition;
		public final JsonObject parameters;

		private MetaModule(Identifier id, Resource resource)
		throws IllegalStateException
		{
			this.id = id;
			var dataResult = DefinitionFromResource(resource);
			if (dataResult.isError()){
				throw new IllegalStateException(dataResult.error().get().message());
			}
			var pair = dataResult.getOrThrow();
			this.definition = pair.first;
			this.parameters = pair.second;

			try {
				this.manager = ModuleRegistry.CreateManager(this.definition, this.parameters);
			}
			catch (IllegalStateException e){
				throw new IllegalStateException(e);
			}
		}
	}

	@Override
	public CompletableFuture<ModuleLoader.Result> load(ResourceManager resourceManager, Executor executor){
		return CompletableFuture.supplyAsync(()->ReloadModules(resourceManager), executor);
	}

	static private ModuleLoader.Result ReloadModules(ResourceManager manager)
	{
		ModuleLoader.Result result = new ModuleLoader.Result();

		for (Map.Entry<Identifier, Resource> entry : manager.findResources("variant-cits/item", id->id.getPath().endsWith(".json")).entrySet())
		{
			Identifier resourceId = entry.getKey();
			MetaModule module;
			try {
				module = new MetaModule(entry.getKey(), entry.getValue());
			}
			catch (IllegalStateException e){
				VariantsCitMod.LOGGER.error("Error in cit module {}: {}", resourceId, e);
				continue;
			}

			module.manager.ReloadVariants(manager);

			Set<RegistryEntry<Item>> targets = module.definition.targets()
				.map(ModuleLoader::ItemsFromTarget)
				.orElseGet(()->ItemsFromResourcePath(resourceId))
				;

			for (var item : targets){
				result.modulesPerItem.computeIfAbsent(item, __->new ArrayList<>()).add(module);
			}
		}

		// Sort highest priorities first.
		for (List<MetaModule> modules : result.modulesPerItem.values()){
			modules.sort((a,b) -> -Integer.compare(a.definition.priority(), b.definition.priority()));
		}
		return result;
	}

	/**
	 * @return An empty optional if the targets were not explicitely defined. An
	 * empty set  if the targets  are explicitely  set, but  do not  contain any
	 * valid item ID.
	 */
	static private Set<RegistryEntry<Item>> ItemsFromTarget(List<Identifier> targets){
		Set<RegistryEntry<Item>> result = new HashSet<>();
		targets.stream()
			.map(id->Registries.ITEM.getEntry(id).get())
			.filter(o->o!=null)
			.forEach(result::add)
			;
		return result;
	}

	static private Set<RegistryEntry<Item>> ItemsFromResourcePath(Identifier resourceId){
		String path = resourceId.getPath();
		path = path.substring("variant-cits/item".length() + 1, path.length() - ".json".length());

		Identifier itemId = Identifier.of(resourceId.getNamespace(), path);

		if (Registries.ITEM.containsId(itemId))
			return Set.of(Registries.ITEM.getEntry(itemId).get());
		else
			return Set.of();
	}

	static private DataResult<Pair<ModuleDefinition, JsonObject>> DefinitionFromResource(Resource resource){
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

			return DataResult.success(Pair.of(definition, parameters));
		}
		catch (IllegalStateException|ClassCastException e){
			return DataResult.error(e::toString);
		}
	}
}
