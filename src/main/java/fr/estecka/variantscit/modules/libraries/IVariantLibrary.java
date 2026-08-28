package fr.estecka.variantscit.modules.libraries;

import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import fr.estecka.variantscit.util.VariantUtil;

public interface IVariantLibrary
{
	static public Identifier FALLBACK_VARIANT_ID = VariantUtil.FALLBACK_VARIANT_ID;

	/**
	 * @return  Whether this variant  has it's own model, ignoring  the fallback
	 * model.
	 */
	public abstract boolean HasVariantModel(Identifier variantId);

	/**
	 * @return The model  that matches  this variant, the  fallback model  if no
	 * model was provided for this variant, or null if the variant is null.
	 */
	public abstract @Nullable Identifier GetVariantModel(Identifier variantId);

	/**
	 * @return The model bound to this variant ID, if any, or null otherwise.
	 */
	public abstract @Nullable Identifier GetVariantModelStrict(Identifier variantId);
}
