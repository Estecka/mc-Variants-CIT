package fr.estecka.variantscit.modules;

import fr.estecka.variantscit.modules.libraries.VariantLibrary;
import net.minecraft.resources.ResourceLocation;

@FunctionalInterface
public interface IModuleBaker<T>
{
	IBakedModule Bake(VariantLibrary library, T parameters);

	default boolean AcceptVariant(ResourceLocation variantId, T parameters){
		return true;
	}
}
