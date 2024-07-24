package fr.estecka.enchantscit.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.render.item.ItemModels;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import fr.estecka.enchantscit.EnchantsCit;

@Mixin(ItemRenderer.class)
public class ItemRendererMixin
{
	@Shadow private @Final ItemModels models;

	@WrapOperation( method="getModel", at=@At( value="INVOKE", target="net/minecraft/client/render/item/ItemModels.getModel (Lnet/minecraft/item/ItemStack;)Lnet/minecraft/client/render/model/BakedModel;") )
	private BakedModel	GetPaintingModel(ItemModels instance, ItemStack stack, Operation<BakedModel> original)
	{
		ItemEnchantmentsComponent enchants;

		if (!stack.isOf(Items.ENCHANTED_BOOK) || (enchants=stack.getEnchantments()).isEmpty())
			return original.call(instance, stack);
		else if (enchants.getSize() > 1)
			return models.getModelManager().getModel(EnchantsCit.MODEL_MIXED);
		else {
			Identifier enchantId = enchants.getEnchantments().iterator().next().getKey().get().getValue();
			return models.getModelManager().getModel(enchantId.withPrefixedPath(EnchantsCit.MODEL_PREFIX));
		}
	}

}
