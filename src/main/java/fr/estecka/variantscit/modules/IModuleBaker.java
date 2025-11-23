package fr.estecka.variantscit.modules;

import fr.estecka.variantscit.modules.libraries.VariantLibrary;
import net.minecraft.util.Identifier;

@FunctionalInterface
public interface IModuleBaker<T>
{
	IBakedModule Bake(VariantLibrary library, T parameters);

	default boolean AcceptVariant(Identifier variantId, T parameters){
		return true;
	}
}
