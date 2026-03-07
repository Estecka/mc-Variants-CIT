package fr.estecka.variantscit.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import fr.estecka.variantscit.VariantsCitMod;
import fr.estecka.variantscit.reload.EModuleHook;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

@Mixin(ItemModelResolver.class)
public class ItemModelManagerMixin
{
	@WrapOperation(
		method="appendItemLayers(Lnet/minecraft/client/renderer/item/ItemStackRenderState;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;I)V",
		at=@At(value="INVOKE", target="net/minecraft/world/item/ItemStack.get(Lnet/minecraft/core/component/DataComponentType;)Ljava/lang/Object;")
	)
	private @Nullable Object GetVariantModel(ItemStack stack, DataComponentType<ResourceLocation> type, Operation<ResourceLocation> original)
	{
		ResourceLocation modelId = null;
		
		VariantsCitMod.LOGGER.PushLabel(stack.getItem());
		modelId = VariantsCitMod.GetModules().GetModelForItem(EModuleHook.ITEM_MODEL, stack);
		VariantsCitMod.LOGGER.PopLabel();

		if (modelId == null)
			return original.call(stack, type);
		else
			return modelId;
	}
}
