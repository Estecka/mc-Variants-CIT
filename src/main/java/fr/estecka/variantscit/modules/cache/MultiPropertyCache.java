package fr.estecka.variantscit.modules.cache;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;
import fr.estecka.variantscit.VariantsCitMod;
import fr.estecka.variantscit.format.properties.IStringProperty;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

/**
 * @deprecated TODO: Merge into CacheModule completely
 */
@Deprecated
public class MultiPropertyCache
implements ICacheableProvider
{
	static public interface ICacheablePropertySource
	{
		/**
		 * Used  for  caching  the variant IDs  associated  with  a given  property.
		 * Typically the hash of {@link #GetReference}.
		 * 
		 * A given  hash can  only be  associated  with a  single  value  of  {@link
		 * #GetPropertyString}. On the  other  hand, the  same  string value  can be
		 * associated with multiple different hashes in cases when the encompassing
		 * component has changed elsewhere.
		 */
		int GetPropertyHash(ItemStack stack);
	
		/**
		 * A direct reference  to the property  or its encompassing object. Used for
		 * clearing caches whenever a property is no longer used anywhere.
		 * 
		 * The  returned  object  should  be  immutable. Like  for  hashes, a  given
		 * identity  must always  correspond  to the  same  return  value of  {@link
		 * #GetPropertyString}, but  the  same  string  value  is allowed  to  match
		 * multiple identities.
		 */
		Object GetReference(ItemStack stack);
	}

	@Deprecated public final boolean debug;
	private final ICacheablePropertySource[] properties;
	private final Int2ObjectMap<CacheEntry> hashToVariant = new Int2ObjectOpenHashMap<>();
	private final ReferenceQueue<Object> expiredComponents = new ReferenceQueue<>();

	public MultiPropertyCache(boolean debug, Stream<? extends ICacheablePropertySource> properties){
		this.debug = debug;
		this.properties = properties.distinct().toArray(ICacheablePropertySource[]::new);
	}

	@Deprecated
	public MultiPropertyCache(boolean debug, IStringProperty... properties){
		this.debug = debug;
		this.properties = Stream.of(properties).map(IStringProperty::GetSource).distinct().toArray(ICacheablePropertySource[]::new);
	}

	public MultiPropertyCache(boolean debug, DataComponentType<?> component){
		this(debug, Stream.of(new CacheableComponent<>(component)));
	}

	@Override
	public Iterable<ICacheablePropertySource> GetProperties() {
		return Set.of(this.properties);
	}

	public ResourceLocation ComputeIfAbsent(ItemStack stack, Function<ItemStack,ResourceLocation> computer){
		this.ExpungeExpiredEntries();

		int hash = this.HashStack(stack);
		CacheEntry entry = this.hashToVariant.get(hash);
		if (entry == null) {
			ResourceLocation variant = computer.apply(stack);
			entry = this.CreateEntry(hash, stack, variant);
			if (debug)
				VariantsCitMod.LOGGER.info("Cache size: {}; Latest Model Id: {}", hashToVariant.size(), String.valueOf(entry.variant));
		}
		return entry.variant;
	}

	/**
	 * @see {@linkplain java.util.Arrays#hashCode(Object[])}
	 */
	private int HashStack(ItemStack stack){
		int hash = 17;
		for (var prop : this.properties){
			hash = hash*31 + prop.GetPropertyHash(stack);
		}
		return hash;
	}

	/**
	 * TODO: As-is, an entry where all registered components are null will never
	 * expire. This is limited to one entry per cache, so it is negligible.
	 */
	private CacheEntry CreateEntry(int hash, ItemStack stack, ResourceLocation variant){
		WeakReference<?>[] weakRefs = new WeakReference[properties.length];

		for (int i=0; i<properties.length; ++i){
			Object ref = properties[i].GetReference(stack);
			if (ref != null)
				weakRefs[i] = new HashedWeakReference(hash, ref, this.expiredComponents);
		}

		CacheEntry entry = new CacheEntry(variant, weakRefs);
		this.hashToVariant.put(hash, entry);
		return entry;
	}

	private void ExpungeExpiredEntries(){
		HashedWeakReference weakRef;
		while ((weakRef=(HashedWeakReference)expiredComponents.poll()) != null){
			this.hashToVariant.remove(weakRef.hash);
		}
	}

	static private class HashedWeakReference
	extends WeakReference<Object>
	{
		/**
		 * The key of the associated entry that must be cleared along with this
		 * reference.
		 */
		public final int hash;

		public HashedWeakReference(int hash, Object referent, ReferenceQueue<Object> queue){
			super(referent, queue);
			this.hash = hash;
		}
	}

	/**
	 * Weak references are kept around so that the weak reference itself doesn't
	 * get garbage collected  before its referee. Otherwise, references will not
	 * get enqueued, and the cache will never be cleared.
	 */
	static private record CacheEntry(ResourceLocation variant, WeakReference<?>[] components)
	{}
}
