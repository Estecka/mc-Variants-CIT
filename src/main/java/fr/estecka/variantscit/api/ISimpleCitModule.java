package fr.estecka.variantscit.api;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

@FunctionalInterface
public interface ISimpleCitModule
extends ICitModule
{
	/**
	 * Identifies  the item's variant, from which the  model ID will be derived.
	 * Items with no variants will fallback to the vanilla model.
	 * 
	 * @return The variant's identifier, or null if the item has none.
	 */
	public abstract @Nullable Identifier GetItemVariant(ItemStack stack);

	@Override
	public default @Nullable ModelIdentifier GetItemModel(ItemStack stack, IVariantManager modelProvider){
		return modelProvider.GetVariantModel(GetItemVariant(stack));
	}
}
