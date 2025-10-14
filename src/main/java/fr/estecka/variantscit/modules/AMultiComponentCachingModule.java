package fr.estecka.variantscit.modules;

import java.util.stream.Stream;
import org.jetbrains.annotations.Nullable;
import fr.estecka.variantscit.MultiPropertyCache;
import fr.estecka.variantscit.api.ICitModule;
import fr.estecka.variantscit.api.IVariantManager;
import fr.estecka.variantscit.format.properties.IStringProperty;
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
 * 
 * TODO: The cache needs to be cleared  after every resource reload. This is not
 * currently enforced; it's expected that the whole module will be discarded and
 * reconstructed during resource-reload.
 */
abstract class AMultiComponentCachingModule
implements ICitModule
{
	protected final boolean debug;
	private final MultiPropertyCache cache;

	protected AMultiComponentCachingModule(boolean debug, Stream<IStringProperty> properties){
		this.debug = debug;
		this.cache = new MultiPropertyCache(properties, debug);
	}

	// TODO: Ensure child classes can't access unregistered non-cached components.
	public abstract @Nullable Identifier RecomputeItemModel(ItemStack stack, IVariantManager library);

	@Override
	public final Identifier GetItemModel(ItemStack stack, IVariantManager library){
		return this.cache.ComputeIfAbsent(stack, s->this.RecomputeItemModel(s, library));
	}
}
