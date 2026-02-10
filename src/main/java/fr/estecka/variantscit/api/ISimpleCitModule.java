package fr.estecka.variantscit.api;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

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
	public abstract @Nullable ResourceLocation GetItemVariant(ItemStack stack);

	@Override
	public default @Nullable ResourceLocation GetItemModel(ItemStack stack, IVariantManager modelProvider){
		return modelProvider.GetVariantModel(GetItemVariant(stack));
	}
}
