package fr.estecka.variantscit;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.function.Function;
import java.util.stream.Stream;
import fr.estecka.variantscit.format.properties.IStringProperty;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.component.ComponentType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class MultiPropertyCache
{
	static public interface ICachableItemProperty
	{
		int GetPropertyHash(ItemStack stack);
		Object GetReference(ItemStack stack);
	}

	public final boolean debug;
	private final IStringProperty[] properties;
	private final Int2ObjectMap<CacheEntry> hashToVariant = new Int2ObjectOpenHashMap<>();
	private final ReferenceQueue<Object> expiredComponents = new ReferenceQueue<>();

	public MultiPropertyCache(boolean debug, Stream<? extends ICachableItemProperty> properties){
		this.debug = debug;
		this.properties = properties.distinct().toArray(IStringProperty[]::new);
	}

	public MultiPropertyCache(boolean debug, ComponentType<?> component){
		this(debug, Stream.of(ComponentProperty(component)));
	}

	static private ICachableItemProperty ComponentProperty(ComponentType<?> type){
		return new ICachableItemProperty() {
			@Override
			public int GetPropertyHash(ItemStack stack) {
				return stack.get(type).hashCode();
			}
			@Override
			public Object GetReference(ItemStack stack) {
				return stack.get(type);
			}
		};
	}

	public Identifier ComputeIfAbsent(ItemStack stack, Function<ItemStack,Identifier> computer){
		this.ExpungeExpiredEntries();

		int hash = this.HashStack(stack);
		CacheEntry entry = this.hashToVariant.get(hash);
		if (entry == null) {
			Identifier variant = computer.apply(stack);
			entry = this.CreateEntry(hash, stack, variant);
			if (debug)
				VariantsCitMod.LOGGER.info("[PropertyCache] Size: {}; Latest Model Id: {}", hashToVariant.size(), String.valueOf(entry.variant));
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
	private CacheEntry CreateEntry(int hash, ItemStack stack, Identifier variant){
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
	static private record CacheEntry(Identifier variant, WeakReference<?>[] components)
	{}
}
