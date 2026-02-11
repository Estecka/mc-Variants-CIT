package fr.estecka.variantscit.reload;

import fr.estecka.variantscit.modules.libraries.VariantLibrary;
import fr.estecka.variantscit.modules.IBakedModule;
import fr.estecka.variantscit.modules.IModuleBaker;
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
