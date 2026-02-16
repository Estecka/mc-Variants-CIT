package fr.estecka.variantscit.modules.cache;

import java.util.Collection;
import fr.estecka.variantscit.modules.cache.MultiPropertyCache.ICacheablePropertySource;

@FunctionalInterface
public interface ICacheSourceProvider
{
	Collection<ICacheablePropertySource> GetCacheSources();
}
