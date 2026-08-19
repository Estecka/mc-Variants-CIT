package fr.estecka.variantscit.modules.libraries;

import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import fr.estecka.variantscit.VariantsCitMod;

public interface IVariantLibrary
{
	static public Identifier FALLBACK_VARIANT_ID = VariantsCitMod.Identifier("fallback");

	static public Identifier SpecialVariantId(String specialName){
		return VariantsCitMod.Identifier("special/"+specialName);
	}

	static public Identifier IntrinsicVariantId(Identifier modelId){
		return VariantsCitMod.Identifier("intrinsic/"+modelId.getNamespace()+"/"+modelId.getPath());
	}

	static public boolean IsVariantIntrinsic(Identifier variantId){
		return variantId.getNamespace().equals(VariantsCitMod.MODID)
		    && variantId.getPath().startsWith("intrinsic/")
		    ;
	}

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
