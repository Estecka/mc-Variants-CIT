package fr.estecka.variantscit.modulebakers;

import fr.estecka.variantscit.VariantLibrary;

@FunctionalInterface
public interface IModuleBaker<T>
{
	IBakedModule Bake(VariantLibrary library, T parameters);
}
