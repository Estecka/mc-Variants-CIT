package fr.estecka.variantscit.reload;

import fr.estecka.variantscit.VariantLibrary;
import fr.estecka.variantscit.modulebakers.IBakedModule;
import fr.estecka.variantscit.modulebakers.IModuleBaker;
import net.minecraft.util.Identifier;

public record UnbakedModule<T>(
	IModuleBaker<T> baker,
	T parameters
){
	public IBakedModule Bake(VariantLibrary library){
		return baker.Bake(library, parameters);
	}

	public boolean AcceptsVariant(Identifier variantId){
		return baker.AcceptVariant(variantId, parameters);
	}
}
