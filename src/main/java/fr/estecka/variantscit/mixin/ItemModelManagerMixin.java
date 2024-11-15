package fr.estecka.variantscit.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.component.ComponentType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import fr.estecka.variantscit.IItemModelProvider;
import fr.estecka.variantscit.VariantsCitMod;

@Mixin(ItemModelManager.class)
public class ItemModelManagerMixin
{
	@WrapOperation(
		method="update(Lnet/minecraft/client/render/item/ItemRenderState;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ModelTransformationMode;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;I)V",
		at=@At(value="INVOKE", target="net/minecraft/item/ItemStack.get(Lnet/minecraft/component/ComponentType;)Ljava/lang/Object;")
	)
	private @Nullable Identifier GetVariantModel(ItemStack stack, ComponentType<Identifier> type, Operation<Identifier> original)
	{
		final IItemModelProvider module = VariantsCitMod.GetModule(stack.getItem());
		Identifier modelId;

		if (module == null || (modelId=module.GetModelForItem(stack)) == null)
			return original.call(stack, type);
		else
			return modelId;
	}
}
