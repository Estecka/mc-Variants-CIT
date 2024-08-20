package fr.estecka.variantscit;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

public class CitLoader
{
	private final String path;
	private final Consumer<Map<Identifier,Identifier>> onModelsCollected;

	public CitLoader(String path, Consumer<Map<Identifier,Identifier>> onModelsCollected){
		this.path = path;
		this.onModelsCollected = onModelsCollected;
	}

	public void FindCITs(ResourceManager manager){
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

		onModelsCollected.accept(models);
	}
}
