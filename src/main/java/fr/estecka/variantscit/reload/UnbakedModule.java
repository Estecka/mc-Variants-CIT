package fr.estecka.variantscit.reload;

import fr.estecka.variantscit.modules.libraries.VariantLibrary;
import net.minecraft.resources.ResourceLocation;
import fr.estecka.variantscit.modules.IBakedModule;
import fr.estecka.variantscit.modules.IModuleBaker;

public record UnbakedModule<T>(
	IModuleBaker<T> baker,
	T parameters
){
	public IBakedModule Bake(VariantLibrary library){
		return baker.Bake(library, parameters);
	}

	public boolean AcceptsVariant(ResourceLocation variantId){
		return baker.AcceptVariant(variantId, parameters);
	}
}
