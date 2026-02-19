package fr.estecka.variantscit.modules.cache;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class CacheKeySet
{
	private final Set<ICacheKey> keyset;

	private CacheKeySet(Set<ICacheKey> keys){
		this.keyset = keys;
	}

	static public CacheKeySet Of(ICacheKey... keys){
		return new CacheKeySet(Set.of(keys));
	}

	static public CacheKeySet Of(Collection<ICacheKey> keys){
		return new CacheKeySet(Set.copyOf(keys));
	}

	static public CacheKeySet Of(Stream<ICacheKey> keys){
		return Of(keys.toList());
	}

	static public CacheKeySet OfCacheables(ICacheKey.Cacheable... cacheable){
		return OfCacheables(List.of(cacheable));
	}

	static public CacheKeySet OfCacheables(Collection<ICacheKey.Cacheable> cacheable){
		CacheKeySet result = CacheKeySet.Of();
		for (var c : cacheable) 
			result = result.Merge(c.GetCacheKeys());
		return result;
	}

	public Stream<ICacheKey> stream(){
		return this.keyset.stream();
	}

	public CacheKeySet Merge(CacheKeySet other){
		Set<ICacheKey> all = new HashSet<>();
		all.addAll(this.keyset);
		all.addAll(other.keyset);
		return new CacheKeySet(all);
	}

	public Comparison CompareTo(CacheKeySet other){
		int matching = 0;
		int missing = 0;
		int superfluous = 0;

		for (ICacheKey key : this.keyset) {
			if (other.keyset.contains(key))
				++matching;
			else
				++superfluous;
		}

		missing = other.keyset.size() - matching;

		return new Comparison(matching, missing, superfluous);
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof CacheKeySet other
		    && this.keyset.equals(other.keyset)
		    ;
	}

	@Override
	public int hashCode() {
		return keyset.hashCode();
	}

	static public record Comparison(
		int matching,
		int missing,
		int superfluous
	) {
		public boolean isEqual()         { return missing == 0 && superfluous == 0; }
		public boolean isSuperset()      { return missing == 0 && superfluous  > 0; }
		public boolean isSuperOrEqual()  { return missing == 0 && superfluous >= 0; }
		public boolean isSubset()        { return missing  > 0 && superfluous == 0; }
		public boolean isSubsetOrEquam() { return missing >= 0 && superfluous == 0; }
		public boolean isMismatch()      { return missing  > 0 && superfluous  > 0; }
		
		public float Similarity() { return matching / (float)(matching + missing + superfluous); }
	}
}
