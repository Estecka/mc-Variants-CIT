package fr.estecka.variantscit.modules.cache;

import java.util.Objects;
import net.minecraft.world.item.ItemStack;

public interface ICacheKey
{
	/**
	 * A direct reference  to the property or its  encompassing object. Used for
	 * clearing caches whenever a property it depends on gets garbage-collected.
	 * The returned object  should be immutable, so as to always  yield the same
	 * hashcode.
	 */
	Object GetReference(ItemStack stack);

	/**
	 * Used  for  caching  the variant IDs  associated  with  a given  property.
	 * Typically the hash of {@link #GetReference}.
	 * 
	 * If {@link #GetReference} returns an object  with a mutable hashcode, hash
	 * it using  {@link System#identityHashCode} instead.
	 */
	default int GetPropertyHash(ItemStack stack){
		return Objects.hashCode(this.GetReference(stack));
	}

	@FunctionalInterface
	static public interface Cacheable
	{
		CacheKeySet GetCacheKeys();
	}

	@FunctionalInterface
	static public interface Keyable
	{
		ICacheKey GetCacheKey();
	}
}
