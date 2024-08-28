package fr.estecka.variantscit;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import org.jetbrains.annotations.Nullable;
import com.google.gson.JsonObject;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.fabricmc.fabric.api.client.model.loading.v1.PreparableModelLoadingPlugin.DataLoader;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class ModuleLoader
implements DataLoader<Map<Item,VariantManager>>
{
	@Override
	public CompletableFuture<Map<Item,VariantManager>> load(ResourceManager resourceManager, Executor executor){
		return CompletableFuture.supplyAsync(()->ReloadModules(resourceManager), executor);
	}

	private Map<Item,VariantManager> ReloadModules(ResourceManager manager){
		Map<Item,VariantManager> result = new HashMap<>();

		for (Map.Entry<Identifier, Resource> entry : manager.findResources("variant-cits/item", id->id.getPath().endsWith(".json")).entrySet())
		{
			Identifier resourceId = entry.getKey();
			Item item = ItemFromResourcePath(resourceId);
			if (item == null)
				continue;

			DataResult<VariantManager> dataResult = ManagerFromResource(entry.getValue());
			if (dataResult.isSuccess()){
				VariantManager module = dataResult.getOrThrow();
				module.ReloadVariants(manager);
				result.put(item, module);
			}
			else {
				VariantsCitMod.LOGGER.error("Error in cit module {}: {}", resourceId, dataResult.error().get().message());
			}
		}

		return result;
	}

	static private @Nullable Item ItemFromResourcePath(Identifier resourceId){
		String path = resourceId.getPath();
		path = path.substring("variant-cits/item".length() + 1, path.length() - ".json".length());

		Identifier itemId = Identifier.of(resourceId.getNamespace(), path);

		if (Registries.ITEM.containsId(itemId))
			return Registries.ITEM.get(itemId);
		else
			return null;
	}

	static private DataResult<VariantManager> ManagerFromResource(Resource resource){
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

			return DataResult.success(ModuleRegistry.CreateManager(definition, parameters));
		}
		catch (IllegalArgumentException|IllegalStateException|ClassCastException e){
			return DataResult.error(e::toString);
		}
	}
}
