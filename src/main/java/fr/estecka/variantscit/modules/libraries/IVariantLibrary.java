package fr.estecka.variantscit.modules.libraries;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import fr.estecka.variantscit.VariantsCitMod;

public interface IVariantLibrary
{
	static public ResourceLocation FALLBACK_VARIANT_ID = VariantsCitMod.Identifier("fallback");

	static public ResourceLocation SpecialVariantId(String specialName){
		return VariantsCitMod.Identifier("special/"+specialName);
	}

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
	public abstract @Nullable ResourceLocation GetVariantModelStrict(ResourceLocation variantId);
}
