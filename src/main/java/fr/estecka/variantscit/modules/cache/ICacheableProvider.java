package fr.estecka.variantscit.modules.cache;

import fr.estecka.variantscit.modules.cache.MultiPropertyCache.ICacheablePropertySource;

@FunctionalInterface
public interface ICacheableProvider
{
	Iterable<ICacheablePropertySource> GetProperties();
}
