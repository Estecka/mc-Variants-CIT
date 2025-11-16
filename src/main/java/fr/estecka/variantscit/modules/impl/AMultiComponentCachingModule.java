package fr.estecka.variantscit.modules.impl;

import java.lang.ref.WeakReference;
import java.lang.ref.ReferenceQueue;
import java.util.stream.Stream;
import org.jetbrains.annotations.Nullable;
import fr.estecka.variantscit.VariantsCitMod;
import fr.estecka.variantscit.api.ICitModule;
import fr.estecka.variantscit.api.IVariantManager;
import fr.estecka.variantscit.format.properties.IStringProperty;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.item.ItemStack;

/**
 * Optimization for deterministic modules that may require expensive computation
 * upon multiple components.
 * 
 * All the relevant components are hashed together, and the resulting variant is
 * cached using  the hash as key. A weak reference is  created for each relevant
 * component, and a cache entry is cleared when at least one of its component is
 * reclaimed by the garbage collector.
 * 
 * TODO: The cache needs to be cleared  after every resource reload. This is not
 * currently enforced; it's expected that the whole module will be discarded and
 * reconstructed during resource-reload.
 */
abstract class AMultiComponentCachingModule
implements ICitModule
{
	protected final boolean debug;
	private final IStringProperty[] properties;

	private final Int2ObjectMap<CacheEntry> hashToVariant = new Int2ObjectOpenHashMap<>();
	private final ReferenceQueue<Object> expiredComponents = new ReferenceQueue<>();

	protected AMultiComponentCachingModule(boolean debug, Stream<IStringProperty> properties){
		this.debug = debug;
		this.properties = properties.distinct().toArray(IStringProperty[]::new);
	}

	// TODO: Ensure child classes can't access unregistered non-cached components.
	public abstract @Nullable ModelIdentifier RecomputeItemModel(ItemStack stack, IVariantManager library);

	@Override
	public final ModelIdentifier GetItemModel(ItemStack stack, IVariantManager library){
		this.ExpungeExpiredEntries();

		int hash = this.HashStack(stack);
		CacheEntry entry = this.hashToVariant.get(hash);
		if (entry == null) {
			entry = this.CreateEntry(hash, stack, library);
			if (debug)
				VariantsCitMod.LOGGER.info("[AMultiComponent] Cache size: {}; Model Id: {}", hashToVariant.size(), String.valueOf(entry.variant));
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
	 * expire. This is limited to one entry per module, so it is negligible.
	 */
	private CacheEntry CreateEntry(int hash, ItemStack stack, IVariantManager library){
		ModelIdentifier variant = this.RecomputeItemModel(stack, library);
		WeakReference<?>[] weakRefs = new WeakReference[properties.length];

		for (int i=0; i<properties.length; ++i){
			Object cmp = properties[i].GetReference(stack);
			if (cmp != null)
				weakRefs[i] = new HashedWeakReference(hash, cmp, this.expiredComponents);
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
	static private record CacheEntry(ModelIdentifier variant, WeakReference<?>[] components)
	{}
}
