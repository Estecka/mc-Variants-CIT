package fr.estecka.variantscit.api;

import org.jetbrains.annotations.Nullable;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

@FunctionalInterface
public interface IVariantManager
{
	/**
	 * @return The model that matches this item's variant, (which may be the 
	 * fallback model), or null if the item has no variant.
	 */
	public abstract @Nullable Identifier GetModelVariantForItem(ItemStack stack);
}
