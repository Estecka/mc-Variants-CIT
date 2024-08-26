package fr.estecka.variantscit.mixin;

import org.spongepowered.asm.mixin.Mixin;
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
import fr.estecka.variantscit.VariantManager;
import fr.estecka.variantscit.VariantsCitMod;

@Mixin(ItemRenderer.class)
@Environment(EnvType.CLIENT)
public class ItemRendererMixin
{
	@WrapOperation( method="getModel", at=@At( value="INVOKE", target="net/minecraft/client/render/item/ItemModels.getModel (Lnet/minecraft/item/ItemStack;)Lnet/minecraft/client/render/model/BakedModel;") )
	private BakedModel	GetVariantModel(ItemModels models, ItemStack stack, Operation<BakedModel> original)
	{
		final VariantManager module = VariantsCitMod.GetModule(stack.getItem());
		ModelIdentifier modelId;

		if (module == null || (modelId=module.GetModelForItem(stack)) == null)
			return original.call(models, stack);

		final BakedModelManager modelManager = models.getModelManager();
		BakedModel model = modelManager.getModel(modelId);
		return (model != null) ? model : modelManager.getMissingModel();
	}

}
