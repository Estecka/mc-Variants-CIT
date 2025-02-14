package fr.estecka.variantscit.modules;

import java.lang.ref.PhantomReference;
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
 * cached using the hash as key. A phantom reference is created for each relevant
 * component, and a cache entry is cleared  when at least one of its component is
 * reclaimed by the garbage collector.
 */
abstract class ASimpleMultiComponentCachingModule
implements ISimpleCitModule
{
	protected final boolean debug;
	private final ComponentType<?>[] componentTypes;

	private final Int2ObjectMap<Identifier> hashToVariant = new Int2ObjectOpenHashMap<>();
	private final ReferenceQueue<Object> phantomQueue = new ReferenceQueue<>();

	protected ASimpleMultiComponentCachingModule(boolean debug, Stream<ComponentType<?>> componentTypes){
		this.debug = debug;
		this.componentTypes = componentTypes.distinct().toArray(ComponentType[]::new);
	}

	public abstract @Nullable Identifier RecomputeItemVariant(ItemStack stack);

	@Override
	public final Identifier GetItemVariant(ItemStack stack){
		this.ExpungeExpiredEntries();

		int hash = this.HashStack(stack);
		Identifier variant = this.hashToVariant.get(hash);
		if (variant == null){
			variant = this.RecomputeItemVariant(stack);
			this.hashToVariant.put(hash, variant);
			this.CreatePhantom(hash, stack);
			if (debug)
				VariantsCitMod.LOGGER.info("[multi_component] Cache size: {}", hashToVariant.size());
		}

		return variant;
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
	 * expire. This is limited to one entry per module, so negligible.
	 */
	private void CreatePhantom(int hash, ItemStack stack){
		for (int i=0; i<this.componentTypes.length; ++i){
			Object cmp = stack.get(this.componentTypes[i]);
			if (cmp != null)
				new HashedPhantomReference(hash, cmp, this.phantomQueue);
		}
	}

	private void ExpungeExpiredEntries(){
		HashedPhantomReference phantom;
		while ((phantom=(HashedPhantomReference)phantomQueue.poll()) != null)
			this.hashToVariant.remove(phantom.hash);
	}

	static private class HashedPhantomReference
	extends PhantomReference<Object>
	{
		/**
		 * The key of the entry associated that must be cleared along with this
		 * reference.
		 */
		public final int hash;

		public HashedPhantomReference(int hash, Object referent, ReferenceQueue<Object> queue){
			super(referent, queue);
			this.hash = hash;
		}
	}
}
