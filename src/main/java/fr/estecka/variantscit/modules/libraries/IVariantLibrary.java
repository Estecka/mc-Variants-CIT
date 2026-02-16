package fr.estecka.variantscit.modules.libraries;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public interface IVariantLibrary
{
	/**
	 * @return  Whether this variant  has it's own model, ignoring  the fallback
	 * model.
	 */
	public abstract boolean HasVariantModel(ResourceLocation variantId);

	/**
	 * @return The model  that matches  this variant, the  fallback model  if no
	 * model was provided for this variant, or null if the variant is null.
	 */
	public abstract @Nullable ResourceLocation GetVariantModel(ResourceLocation variantId);

	/**
	 * @return The special model that was provided for this key.
	 */
	public abstract @Nullable ResourceLocation GetSpecialModel(String key);
}
