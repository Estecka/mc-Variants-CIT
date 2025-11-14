package fr.estecka.variantscit.mixin;

import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import fr.estecka.variantscit.reload.ModuleLoader;
import fr.estecka.variantscit.reload.VariantAggregator;
import fr.estecka.variantscit.VariantsCitMod;
import net.minecraft.client.item.ItemAsset;
import net.minecraft.client.item.ItemAssetsLoader;
import net.minecraft.client.render.item.model.BasicItemModel;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.resource.ResourceReloader;
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

	static private ItemAsset ItemFromModel(Identifier assetId) {
		var unbaked = new BasicItemModel.Unbaked(assetId, List.of());
		var properties = new ItemAsset.Properties(true, false);
		return new ItemAsset(unbaked, properties);
	}

	@Inject( method="reload", at=@At("HEAD") )
	private void reload(CallbackInfoReturnable<?> ci, @Local(argsOnly=true) ResourceReloader.Store store, @Share("result") LocalRef<VariantAggregator> resultRef){
		ModuleLoader.Result result = ModuleLoader.ReloadModules(store.getResourceManager());
		resultRef.set(result.variantAggregator);
		VariantsCitMod.OnResourceReload(result);
	}

	@ModifyExpressionValue( method="reload", at=@At(value="INVOKE", target="net/minecraft/client/render/model/BakedModelManager.reloadModels(Lnet/minecraft/resource/ResourceManager;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"))
	static private CompletableFuture<Map<Identifier,UnbakedModel>> AddVariantModels(CompletableFuture<Map<Identifier,UnbakedModel>> original, @Share("result") LocalRef<VariantAggregator> resultRef) {
		return original.thenApply( (allModels)->{
			allModels = new HashMap<Identifier, UnbakedModel>(allModels);
	
			Map<Identifier,VariantAggregator.ModelToCreate> models = resultRef.get().modelsToCreate;
			VariantsCitMod.LOGGER.info("Creating {} models from textures...", models.size());
			for (var entry : models.entrySet()){
				Identifier assetId = entry.getKey().withPrefixedPath("item/");
				allModels.put(assetId, ModelFromTexture(assetId, entry.getValue().parent()));
			}
	
			return allModels;
		});
	}

	@ModifyExpressionValue( method="reload", at=@At(value="INVOKE", target="net/minecraft/client/item/ItemAssetsLoader.load(Lnet/minecraft/resource/ResourceManager;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"))
	static private CompletableFuture<ItemAssetsLoader.Result> AddVariantItems(CompletableFuture<ItemAssetsLoader.Result> original, @Share("result") LocalRef<VariantAggregator> resultRef) {
		return original.thenApply( (result)->{
			var allItems = new HashMap<Identifier, ItemAsset>(result.contents());
		
			Set<Identifier> items = resultRef.get().itemStatesToCreate;
			VariantsCitMod.LOGGER.info("Creating {} items from models...", items.size());
			for (Identifier assetId : items){
				allItems.put(assetId, ItemFromModel(assetId.withPrefixedPath("item/")));
			}
			return new ItemAssetsLoader.Result(allItems);
		});
	}
}
