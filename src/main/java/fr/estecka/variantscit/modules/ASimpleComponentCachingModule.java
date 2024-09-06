package fr.estecka.variantscit.modules;

import java.util.WeakHashMap;
import org.jetbrains.annotations.Nullable;
import fr.estecka.variantscit.api.ISimpleCitModule;
import net.minecraft.component.ComponentType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

/**
 * Optimization for deterministic modules that may require expensive computation
 * upon a single item component.
 * 
 * The cache in this version is allowed to survive resource reloads.
 */
abstract class ASimpleComponentCachingModule<T>
implements ISimpleCitModule
{
	protected final ComponentType<T> componentType;

	/*
	 * The lifetime of each entry  is roughly equivalent  to the lifetime of the
	 * associated  item stack's  component. Item components  are supposed  to be
	 * immutable, so a cache  should never  need  to be  recomputed  for a given
	 * identity.
	 */
	private final WeakHashMap<T, @Nullable Identifier> cachedVariants = new WeakHashMap<>();

	public ASimpleComponentCachingModule(ComponentType<T> component){
		this.componentType = component;
	}


	@Override
	public final Identifier GetItemVariant(ItemStack stack){
		T component = stack.get(this.componentType);
		if (component == null)
			return null;

		/**
		 * Do  not  use  computeIfAbsent! It would  attempt  to  recompute  null
		 * values, which are valid to cache.
		 */
		if (!cachedVariants.containsKey(component))
			cachedVariants.put(component, GetVariantForComponent(component));

		return cachedVariants.get(component);
	}

	public abstract Identifier GetVariantForComponent(T component);
}
