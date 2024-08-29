package fr.estecka.variantscit.api;

import org.jetbrains.annotations.Nullable;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public interface IVariantManager
{
	/**
	 * @deprecated  Will be removed alongside {@link ICitModule#GetItemVariant}.
	 * Use {@link #GetModelForVariant} instead.
	 */
	@Deprecated
	public abstract @Nullable ModelIdentifier GetModelVariantForItem(ItemStack stack);

	/**
	 * @return The model  that matches  this variant, the  fallback model  if no
	 * model was provided for this variant, or null if the variant is null.
	 */
	public abstract @Nullable ModelIdentifier GetVariantModel(Identifier variantId);

	/**
	 * @return The special model that was provided for this key.
	 */
	public abstract @Nullable ModelIdentifier GetSpecialModel(String key);
}
