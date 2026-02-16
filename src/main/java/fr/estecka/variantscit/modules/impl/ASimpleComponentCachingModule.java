package fr.estecka.variantscit.modules.impl;

import java.util.WeakHashMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import fr.estecka.variantscit.modules.libraries.ISimpleCitModule;
import fr.estecka.variantscit.modules.libraries.IVariantLibrary;
import fr.estecka.variantscit.commands.CommandLogger;

/**
 * Optimization for deterministic modules that may require expensive computation
 * upon a single item component.
 * 
 * The cache in this version is allowed to survive resource reloads.
 */
abstract class ASimpleComponentCachingModule<T>
implements ISimpleCitModule
{
	protected final DataComponentType<T> componentType;

	/*
	 * The lifetime of each entry  is roughly equivalent  to the lifetime of the
	 * associated  item stack's  component. Item components  are supposed  to be
	 * immutable, so a variant  should never need  to be recomputed  for a given
	 * component.
	 */
	final WeakHashMap<T, @Nullable ResourceLocation> cachedVariants = new WeakHashMap<>();

	public ASimpleComponentCachingModule(DataComponentType<T> component){
		this.componentType = component;
	}


	@Override
	public final ResourceLocation GetItemVariant(ItemStack stack){
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

	@Override
	public @Nullable ResourceLocation Walkthrough(ItemStack stack, IVariantLibrary library, CommandLogger logger) {
		T component = stack.get(this.componentType);
		if (component == null)
			return null;

		return library.GetVariantModel(GetVariantForComponent(component));
	}

	public abstract ResourceLocation GetVariantForComponent(T component);
}
