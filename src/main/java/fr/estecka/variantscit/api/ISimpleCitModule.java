package fr.estecka.variantscit.api;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

@FunctionalInterface
public interface ISimpleCitModule
extends ICitModule
{
	public abstract @Nullable Identifier GetItemVariant(ItemStack stack);

	@Override
	public default @Nullable ModelIdentifier GetItemModel(ItemStack stack, IVariantManager variantBasedModel){
		return variantBasedModel.GetModelVariantForItem(stack);
	}
}
