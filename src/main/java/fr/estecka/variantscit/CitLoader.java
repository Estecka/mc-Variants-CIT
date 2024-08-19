package fr.estecka.variantscit;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.PreparableModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.PreparableModelLoadingPlugin.DataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;


public class CitLoader
implements PreparableModelLoadingPlugin<Map<Identifier,Identifier>>, DataLoader<Map<Identifier,Identifier>>
{
	private final String path;
	private final Consumer<Map<Identifier,Identifier>> onModelsCollected;

	public CitLoader(String path, Consumer<Map<Identifier,Identifier>> onModelsCollected){
		this.path = path;
		this.onModelsCollected = onModelsCollected;
	}

	@Override
	public void onInitializeModelLoader(Map<Identifier,Identifier> variantsToModelId, ModelLoadingPlugin.Context pluginContext){
		VariantsCitMod.LOGGER.info("Found {} CITs for {}", variantsToModelId.size(), path);
		pluginContext.addModels(variantsToModelId.values());
		onModelsCollected.accept(variantsToModelId);
	}

	@Override
	public CompletableFuture<Map<Identifier,Identifier>> load(ResourceManager manager, Executor executor){
		return CompletableFuture.supplyAsync(()->FindCITs(manager), executor);
	}

	private Map<Identifier, Identifier> FindCITs(ResourceManager manager){
		Map<Identifier,Identifier> models = new HashMap<>();

		String folder = "models/"+path;

		for (Identifier fileId : manager.findResources(folder, id -> id.getPath().endsWith(".json")).keySet())
		{
			String namespace = fileId.getNamespace();
			String modelName, variantName;

			modelName = fileId.getPath();
			modelName = modelName.substring("models/".length(), modelName.length()-".json".length());

			variantName = modelName.substring(path.length() + 1);

			models.put(
				Identifier.of(namespace, variantName),
				Identifier.of(namespace, modelName)
			);
		}

		return models;
	}
}
