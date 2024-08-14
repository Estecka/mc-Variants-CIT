package fr.estecka.enchantscit.mixin;

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
	@WrapOperation( method="getModel", at=@At( value="INVOKE", target="net/minecraft/client/render/item/ItemModels.getModel (Lnet/minecraft/item/ItemStack;)Lnet/minecraft/client/render/model/BakedModel;") )
	private BakedModel	GetEnchantModel(ItemModels models, ItemStack stack, Operation<BakedModel> original)
	{
		final BakedModelManager modelManager = models.getModelManager();
		ItemEnchantmentsComponent enchants;
		BakedModel model;

		if (!stack.isOf(Items.ENCHANTED_BOOK) || null == (enchants=stack.get(DataComponentTypes.STORED_ENCHANTMENTS)) || enchants.isEmpty())
			return original.call(models, stack);
		else if (enchants.getSize() > 1){
			model = modelManager.getModel(EnchantsCit.CIT_MULTI);
			return (model != null) ? model : modelManager.getMissingModel();
		}
		else {
			Identifier enchantId = enchants.getEnchantments().iterator().next().getKey().get().getValue();
			// Can return a missing model with ModernFix, or null otherwise.
			model = modelManager.getModel(EnchantsCit.OfVariant(enchantId));
			return (model!=null && model!=modelManager.getMissingModel()) ? model : original.call(models, stack);
		}
	}

}
