package fr.estecka.variantscit.mixin;

import java.util.function.Supplier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.item.ItemModels;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.item.ItemStack;
import fr.estecka.variantscit.IItemModelProvider;
import fr.estecka.variantscit.VariantsCitMod;

@Mixin(ItemRenderer.class)
@Environment(EnvType.CLIENT)
public class ItemRendererMixin
{
	@Shadow private @Final BakedModelManager bakedModelManager;

	@Unique
	private BakedModel	GetVariantModel(BakedModelManager modelManager, ItemStack stack, Supplier<BakedModel> original)
	{
		final IItemModelProvider module = VariantsCitMod.GetModule(stack.getItem());
		ModelIdentifier modelId;

		if (module == null || (modelId=module.GetModelForItem(stack)) == null)
			return original.get();

		BakedModel model = modelManager.getModel(modelId);
		return (model != null) ? model : modelManager.getMissingModel();
	}

	// Most items
	@WrapOperation( method="getModel", require=1, at=@At( value="INVOKE", target="net/minecraft/client/render/item/ItemModels.getModel (Lnet/minecraft/item/ItemStack;)Lnet/minecraft/client/render/model/BakedModel;") )
	private BakedModel	GetItemStackModel(ItemModels models, ItemStack stack, Operation<BakedModel> original) {
		return GetVariantModel(bakedModelManager, stack, ()->original.call(models, stack));
	}

	// Trident and Spyglass
	@WrapOperation(
		// Why are there so many different overloads of renderItem ?!?
		method="renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/render/model/BakedModel;Z)V",
		require=2, at=@At( value="INVOKE", target="net/minecraft/client/render/model/BakedModelManager.getModel (Lnet/minecraft/client/util/ModelIdentifier;)Lnet/minecraft/client/render/model/BakedModel;")
	)
	private BakedModel	GetIdentifiedModel(BakedModelManager models, ModelIdentifier id, Operation<BakedModel> original, ItemStack stack) {
		return GetVariantModel(models, stack, ()->original.call(models, id));
	}

}
