package fr.estecka.variantscit.mixin;

import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import fr.estecka.variantscit.reload.ModuleLoader;
import fr.estecka.variantscit.VariantsCitMod;
import net.minecraft.client.model.ItemAssetsLoader;
import net.minecraft.client.render.item.model.BasicItemModel;
import net.minecraft.client.render.item.model.ItemModel;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

@Unique
@Mixin(BakedModelManager.class)
public class BakedModelManagerMixin
{
	/**
	 * I could probably use the constructor for JsonUnbakedModel instead, but it
	 * is unclear how parent-child inheritance works with it.
	 */
	static private final String ARBITRARY_MODEL = """
		{
			"parent": "%s",
			"textures": {
				"layer0": "%s"
			}
		}
	""";

	static private UnbakedModel ModelFromTexture(Identifier assetId, Identifier parent) {
		StringReader reader = new StringReader(ARBITRARY_MODEL.formatted(parent.toString(), assetId.toString()));
		JsonUnbakedModel model = JsonUnbakedModel.deserialize(reader);
		return model;
	}

	static private ItemModel.Unbaked ItemFromModel(Identifier assetId) {
		return new BasicItemModel.Unbaked(assetId.withPrefixedPath("item/"), List.of());
	}

	@Inject( method="reload", at=@At("HEAD") )
	private void reload(CallbackInfoReturnable<?> ci, @Local ResourceManager manager){
		new VariantsCitMod().initialize(ModuleLoader.ReloadModules(manager));
	}

	@ModifyExpressionValue( method="reload", at=@At(value="INVOKE", target="net/minecraft/client/render/model/BakedModelManager.reloadModels(Lnet/minecraft/resource/ResourceManager;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"))
	static private CompletableFuture<Map<Identifier,UnbakedModel>> AddVariantModels(CompletableFuture<Map<Identifier,UnbakedModel>> original) {
		return original.thenApply( (allModels)->{
			allModels = new HashMap<Identifier, UnbakedModel>(allModels);
	
			var models = VariantsCitMod.GetModelsToCreate();
			VariantsCitMod.LOGGER.info("Creating {} models from textures...", models.size());
			for (var entry : models.entrySet()){
				Identifier resourceId = entry.getKey().withPrefixedPath("item/");
				allModels.put(resourceId, ModelFromTexture(resourceId, entry.getValue()));
			}
	
			return allModels;
		});
	}

	@ModifyExpressionValue( method="reload", at=@At(value="INVOKE", target="net/minecraft/client/model/ItemAssetsLoader.load(Lnet/minecraft/resource/ResourceManager;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"))
	static private CompletableFuture<ItemAssetsLoader.Result> AddVariantItems(CompletableFuture<ItemAssetsLoader.Result> original) {
		return original.thenApply( (result)->{
			var allItems = new HashMap<Identifier, ItemModel.Unbaked>(result.models());
		
			var items = VariantsCitMod.GetItemsToCreate();
			VariantsCitMod.LOGGER.info("Creating {} items from models...", items.size());
			for (Identifier assetId : items){
				allItems.put(assetId, ItemFromModel(assetId));
			}
			return new ItemAssetsLoader.Result(allItems);
		});
	}
}
