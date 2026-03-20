package fr.estecka.variantscit.modules.libraries;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface ISimpleCitModule
extends IVariantCitModule
{
	/**
	 * Identifies  the item's variant, from which the  model ID will be derived.
	 * Items with no variants will fallback to the vanilla model.
	 * 
	 * @return The variant's identifier, or null if the item has none.
	 */
	public abstract @Nullable ResourceLocation GetItemVariant(ItemStack stack);

	@Override
	public default @Nullable ResourceLocation GetItemModel(ItemStack stack, IVariantLibrary library){
		return library.GetVariantModel(GetItemVariant(stack));
	}
}
