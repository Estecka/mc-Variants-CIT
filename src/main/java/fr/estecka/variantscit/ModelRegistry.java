package fr.estecka.variantscit;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import fr.estecka.variantscit.api.IVariantsCitModule;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.PreparableModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.PreparableModelLoadingPlugin.DataLoader;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;


public class ModelRegistry
implements PreparableModelLoadingPlugin<Map<Identifier,Identifier>>, DataLoader<Map<Identifier,Identifier>>
{
	public final IVariantsCitModule module;

	/**
	 * Maps item variants to model ids.
	 */
	private @NotNull Map<Identifier, Identifier> existingModels = new HashMap<>();

	public ModelRegistry(IVariantsCitModule module){;
		this.module = module;
	}


/******************************************************************************/
/* # Rendering                                                                */
/******************************************************************************/

	/**
	 * @return null if the model should be vanilla.
	 */
	public @Nullable BakedModel GetModelForItem(ItemStack stack, BakedModelManager manager){
		
		Identifier modelId = module.GetExceptionalModel(stack);
		if (modelId == null){
			Identifier variantId = module.GetItemVariant(stack);
			if (variantId == null)
				return null;
			else {
				modelId = existingModels.get(variantId);
				if (modelId == null)
					modelId = module.GetFallbackModel();
			}
		}

		BakedModel model = manager.getModel(modelId);
		return (model != null) ? model : manager.getMissingModel();

	}

/******************************************************************************/
/* # Resource Loading                                                         */
/******************************************************************************/

	@Override
	public void onInitializeModelLoader(Map<Identifier,Identifier> enchantIds, ModelLoadingPlugin.Context pluginContext){
		pluginContext.addModels(enchantIds.values());
		this.existingModels = enchantIds;

		VariantsCitMod.LOGGER.info("Found {} CITs for {}", module.GetModelPath());
	}

	@Override
	public CompletableFuture<Map<Identifier,Identifier>> load(ResourceManager manager, Executor executor){
		return CompletableFuture.supplyAsync(()->FindCITs(manager), executor);
	}

	private Map<Identifier, Identifier> FindCITs(ResourceManager manager){
		Map<Identifier,Identifier> models = new HashMap<>();

		final String path = module.GetModelPath();
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
