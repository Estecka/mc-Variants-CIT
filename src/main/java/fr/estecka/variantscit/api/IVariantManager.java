package fr.estecka.variantscit.api;

import org.jetbrains.annotations.Nullable;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public interface IVariantManager
{
	/**
	 * @deprecated  Will be removed alongside {@link ICitModule#GetItemVariant}.
	 * Use {@link #GetVariantModel} instead.
	 */
	@Deprecated
	public abstract @Nullable ModelIdentifier GetModelVariantForItem(ItemStack stack);

	/**
	 * @return  Whether this variant  has it's own model, ignoring  the fallback
	 * model.
	 */
	public abstract boolean HasVariantModel(Identifier variantId);

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
