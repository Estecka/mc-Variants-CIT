package fr.estecka.variantscit.modules.impl;

import java.util.WeakHashMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import fr.estecka.variantscit.VariantsCitMod;
import fr.estecka.variantscit.api.ICitModule;
import fr.estecka.variantscit.api.IVariantManager;
import fr.estecka.variantscit.commands.CommandLogger;

/**
 * Optimization for deterministic modules that may require expensive computation
 * upon a single item component.
 * 
 * This version  may be  less stable  than  its simple  counterpart, because its
 * cache needs to be cleared after every resource reload.
 */
abstract class AComponentCachingModule<T>
implements ICitModule
{
	protected final DataComponentType<T> componentType;

	/*
	 * The lifetime of each entry  is roughly equivalent  to the lifetime of the
	 * associated  item stack's  component. Item components  are supposed  to be
	 * immutable, so a cache  should never  need  to be  recomputed  for a given
	 * identity.
	 */
	private final WeakHashMap<T, @Nullable ResourceLocation> cachedModels = new WeakHashMap<>();
	private int reloadCounts = 0;

	public AComponentCachingModule(DataComponentType<T> component){
		this.componentType = component;
	}


	@Override
	public final ResourceLocation GetItemModel(ItemStack stack, IVariantManager models){
		T component = stack.get(this.componentType);
		if (component == null)
			return null;

		if (this.reloadCounts != VariantsCitMod.reloadcount){
			this.reloadCounts = VariantsCitMod.reloadcount;
			cachedModels.clear();
		}
		/**
		 * Do  not  use  computeIfAbsent! It would  attempt  to  recompute  null
		 * values, which are valid to cache.
		 */
		if (!cachedModels.containsKey(component))
			cachedModels.put(component, GetModelForComponent(component, models));

		return cachedModels.get(component);
	}

	@Override
	public @Nullable ResourceLocation Walkthrough(ItemStack stack, IVariantManager library, CommandLogger logger) {
		T component = stack.get(this.componentType);
		if (component == null)
			return null;

		return GetModelForComponent(component, library);
	}

	public abstract ResourceLocation GetModelForComponent(T component, IVariantManager models);
}
