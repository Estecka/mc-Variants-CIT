package fr.estecka.variantscit.modules;

import java.lang.ref.WeakReference;
import java.lang.ref.ReferenceQueue;
import java.util.stream.Stream;
import org.jetbrains.annotations.Nullable;
import fr.estecka.variantscit.VariantsCitMod;
import fr.estecka.variantscit.api.ISimpleCitModule;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.component.ComponentType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

/**
 * Optimization for deterministic modules that may require expensive computation
 * upon multiple components.
 * 
 * All the relevant components are hashed together, and the resulting variant is
 * cached using  the hash as key. A weak reference is  created for each relevant
 * component, and a cache entry is cleared when at least one of its component is
 * reclaimed by the garbage collector.
 */
abstract class ASimpleMultiComponentCachingModule
implements ISimpleCitModule
{
	protected final boolean debug;
	private final ComponentType<?>[] componentTypes;

	private final Int2ObjectMap<CacheEntry> hashToVariant = new Int2ObjectOpenHashMap<>();
	private final ReferenceQueue<Object> expiredComponents = new ReferenceQueue<>();

	protected ASimpleMultiComponentCachingModule(boolean debug, Stream<ComponentType<?>> componentTypes){
		this.debug = debug;
		this.componentTypes = componentTypes.distinct().toArray(ComponentType[]::new);
	}

	// TODO: Ensure child classes can't access unregistered non-cached components.
	public abstract @Nullable Identifier RecomputeItemVariant(ItemStack stack);

	@Override
	public final Identifier GetItemVariant(ItemStack stack){
		this.ExpungeExpiredEntries();

		int hash = this.HashStack(stack);
		CacheEntry entry = this.hashToVariant.get(hash);
		if (entry == null) {
			entry = this.CreateEntry(hash, stack);
			if (debug)
				VariantsCitMod.LOGGER.info("[multi_component] Cache size: {}", hashToVariant.size());
		}
		return entry.variant;
	}


	/**
	 * @see {@linkplain java.util.Arrays#hashCode(Object[])}
	 */
	private int HashStack(ItemStack stack){
		int hash = 17;
		for (var type : this.componentTypes){
			Object cmp = stack.get(type);
			hash = hash*31 + ((cmp!=null) ? cmp.hashCode() : 0);
		}
		return hash;
	}

	/**
	 * TODO: As-is, an entry where all registered components are null will never
	 * expire. This is limited to one entry per module, so it is negligible.
	 */
	private CacheEntry CreateEntry(int hash, ItemStack stack){
		Identifier variant = this.RecomputeItemVariant(stack);
		WeakReference<?>[] weakRefs = new WeakReference[componentTypes.length];

		for (int i=0; i<componentTypes.length; ++i){
			Object cmp = stack.get(componentTypes[i]);
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
	static private record CacheEntry(Identifier variant, WeakReference<?>[] components)
	{}
}
