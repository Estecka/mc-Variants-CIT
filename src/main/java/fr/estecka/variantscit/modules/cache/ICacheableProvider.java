package fr.estecka.variantscit.modules.cache;

import fr.estecka.variantscit.modules.cache.MultiPropertyCache.ICachableItemProperty;

@FunctionalInterface
public interface ICacheableProvider
{
	Iterable<ICachableItemProperty> GetProperties();
}
