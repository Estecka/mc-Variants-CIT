package fr.estecka.variantscit.modules;

import java.lang.ref.PhantomReference;
import java.util.stream.Stream;
import org.jetbrains.annotations.Nullable;
import fr.estecka.variantscit.VariantsCitMod;
import fr.estecka.variantscit.api.ISimpleCitModule;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.component.ComponentType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

abstract class ASimpleMultiComponentCachingModule
implements ISimpleCitModule
{
	protected final boolean debug;
	private final ComponentType<?>[] componentTypes;
	private final Int2ObjectMap<CacheEntry> cache = new Int2ObjectOpenHashMap<>();

	protected ASimpleMultiComponentCachingModule(boolean debug, Stream<ComponentType<?>> componentTypes){
		this.debug = debug;
		this.componentTypes = componentTypes.distinct().toArray(ComponentType[]::new);
	}

	@Override
	public final Identifier GetItemVariant(ItemStack stack){
		this.cache.values().removeIf(CacheEntry::isExpired);

		int hash = this.HashStack(stack);
		CacheEntry entry = this.cache.get(hash);
		if (entry == null){
			entry = this.CreateCacheEntry(stack);
			this.cache.put(hash, entry);
			if (debug)
				VariantsCitMod.LOGGER.info("Cache size: {}", cache.size());
		}

		return entry.variant;
	}

	public abstract @Nullable Identifier RecomputeItemVariant(ItemStack stack);

	private CacheEntry CreateCacheEntry(ItemStack stack){
		Identifier variant = this.RecomputeItemVariant(stack);
		PhantomReference<?>[] components = new PhantomReference[this.componentTypes.length];
		CacheEntry entry = new CacheEntry(variant, components);

		for (int i=0; i<components.length; ++i){
			Object cmp = stack.get(this.componentTypes[i]);
			components[i] = (cmp != null) ? new PhantomReference<>(cmp, null) : null;
			cmp = null;
		}

		return entry;
	}

	/**
	 * @see {@linkplain java.util.Arrays#hashCode(Object[])}
	 */
	private int HashStack(ItemStack stack){
		int hash = 17;
		for (var type : this.componentTypes){
			Object cmp = stack.get(type);
			// hash = hash*31 + ((cmp!=null) ? System.identityHashCode(cmp) : 0);
			hash = hash*31 + ((cmp!=null) ? cmp.hashCode() : 0);
		}
		return hash;
	}

	/**
	 * TODO: As-is, an entry where all components are null will never expire.
	 */
	static private record CacheEntry(
		Identifier variant,
		PhantomReference<?>[] components
	) {
		public boolean isExpired(){
			for (var ref : this.components)
				if (ref != null && ref.refersTo(null))
					return true;

			return false;
		}
	}
}
