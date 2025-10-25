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
import fr.estecka.variantscit.VariantsCitMod;
import fr.estecka.variantscit.modulebakers.IBakedModule;

@Mixin(ItemModelManager.class)
public class ItemModelManagerMixin
{
	@WrapOperation(
		method="update",
		at=@At(value="INVOKE", target="net/minecraft/item/ItemStack.get(Lnet/minecraft/component/ComponentType;)Ljava/lang/Object;")
	)
	private @Nullable Object GetVariantModel(ItemStack stack, ComponentType<Identifier> type, Operation<Identifier> original)
	{
		final IBakedModule module = VariantsCitMod.GetItemModule(stack.getItem());
		Identifier modelId = null;

		if (module != null){
			VariantsCitMod.LOGGER.PushLabel(stack.getItem());
			modelId = module.GetModelForItem(stack);
			VariantsCitMod.LOGGER.PopLabel();
		}

		if (modelId == null)
			return original.call(stack, type);
		else
			return modelId;
	}
}
