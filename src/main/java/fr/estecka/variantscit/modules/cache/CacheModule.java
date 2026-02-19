package fr.estecka.variantscit.modules.cache;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.function.Function;
import fr.estecka.variantscit.modules.IBakedModule;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;


public class CacheModule
implements IBakedModule
{
	private final ICacheKey[] properties;
	private final Int2ObjectMap<CacheEntry> hashToVariant = new Int2ObjectOpenHashMap<>();
	private final ReferenceQueue<Object> expiredComponents = new ReferenceQueue<>();

	private final IBakedModule inner;

	public CacheModule(IBakedModule inner){
		this.inner = inner;
		this.properties = inner.GetCacheKeys().stream().distinct().toArray(ICacheKey[]::new);
	}

	@Override
	public CacheKeySet GetCacheKeys() {
		return CacheKeySet.Of(this.properties);
	}

	@Override
	public ECachePolicy GetCachePolicy() {
		return ECachePolicy.UNWRAP;
	}

	@Override
	public ResourceLocation GetModelForItem(ItemStack stack) {
		this.ExpungeExpiredEntries();

		int hash = this.HashStack(stack);
		CacheEntry entry = this.hashToVariant.get(hash);
		if (entry == null) {
			ResourceLocation variant = inner.GetModelForItem(stack);
			entry = this.CreateEntry(hash, stack, variant);
		}
		return entry.variant;
	}

	public ResourceLocation ComputeIfAbsent(ItemStack stack, Function<ItemStack,ResourceLocation> computer){
		this.ExpungeExpiredEntries();

		int hash = this.HashStack(stack);
		CacheEntry entry = this.hashToVariant.get(hash);
		if (entry == null) {
			ResourceLocation variant = computer.apply(stack);
			entry = this.CreateEntry(hash, stack, variant);
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
