package fr.estecka.variantscit.modulebakers;

import fr.estecka.variantscit.IItemModelProvider;
import fr.estecka.variantscit.api.IVariantManager;

@FunctionalInterface
public interface IModuleBaker<T>
{
	IItemModelProvider Bake(IVariantManager library, T logic);
}
