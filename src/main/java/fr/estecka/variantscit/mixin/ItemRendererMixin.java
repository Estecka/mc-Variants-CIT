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
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import fr.estecka.variantscit.VariantsCitMod;
import fr.estecka.variantscit.api.ACitModule;

@Mixin(ItemRenderer.class)
@Environment(EnvType.CLIENT)
public class ItemRendererMixin
{
	@WrapOperation( method="getModel", at=@At( value="INVOKE", target="net/minecraft/client/render/item/ItemModels.getModel (Lnet/minecraft/item/ItemStack;)Lnet/minecraft/client/render/model/BakedModel;") )
	private BakedModel	GetEnchantModel(ItemModels models, ItemStack stack, Operation<BakedModel> original)
	{
		final ACitModule module = VariantsCitMod.GetModule(stack.getItem());
		Identifier modelId;

		if (module == null || (modelId=module.GetModelForItem(stack)) == null)
			return original.call(models, stack);

		final BakedModelManager modelManager = models.getModelManager();
		BakedModel model = models.getModelManager().getModel(modelId);
		if (model != null) 
			return model;
		else
			return modelManager.getMissingModel();
	}

}
