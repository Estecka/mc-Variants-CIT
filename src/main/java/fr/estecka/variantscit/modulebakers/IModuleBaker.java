package fr.estecka.variantscit.modulebakers;

import fr.estecka.variantscit.api.IVariantManager;

@FunctionalInterface
public interface IModuleBaker<T>
{
	IBakedModule Bake(IVariantManager library, T parameters);
}
