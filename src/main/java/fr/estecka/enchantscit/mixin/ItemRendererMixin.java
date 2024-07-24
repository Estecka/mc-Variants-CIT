package fr.estecka.enchantscit.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.item.ItemModels;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import fr.estecka.enchantscit.EnchantsCit;

@Mixin(ItemRenderer.class)
@Environment(EnvType.CLIENT)
public class ItemRendererMixin
{
	@Shadow private @Final ItemModels models;

	@WrapOperation( method="getModel", at=@At( value="INVOKE", target="net/minecraft/client/render/item/ItemModels.getModel (Lnet/minecraft/item/ItemStack;)Lnet/minecraft/client/render/model/BakedModel;") )
	private BakedModel	GetPaintingModel(ItemModels instance, ItemStack stack, Operation<BakedModel> original)
	{
		final BakedModelManager modelManager = models.getModelManager();
		ItemEnchantmentsComponent enchants;
		BakedModel model;


		if (!stack.isOf(Items.ENCHANTED_BOOK) || null == (enchants=stack.get(DataComponentTypes.STORED_ENCHANTMENTS)) || enchants.isEmpty())
			return original.call(instance, stack);
		else if (enchants.getSize() > 1){
			model = modelManager.getModel(EnchantsCit.CIT_MIXED);
			return (model != null) ? model : modelManager.getMissingModel();
		}
		else {
			Identifier enchantId = enchants.getEnchantments().iterator().next().getKey().get().getValue();
			model = modelManager.getModel(EnchantsCit.OfVariant(enchantId));
			return (model != null) ? model : original.call(instance, stack);
		}
	}

}
