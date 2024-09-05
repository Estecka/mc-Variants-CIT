package fr.estecka.variantscit.modules;

import java.util.WeakHashMap;
import org.jetbrains.annotations.Nullable;
import fr.estecka.variantscit.api.ICitModule;
import fr.estecka.variantscit.api.IVariantManager;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.component.ComponentType;
import net.minecraft.item.ItemStack;

/**
 * Optimization for deterministic modules that may require expensive computation
 * upon a single item component.
 * 
 * TODO:
 * Clear  the cache  upon  reosurce  reload.  This  does not  work  well  on the
 * EnchantedToolModule because  it both survives the reload, and caches based on 
 * available models.
 */
abstract class AComponentCachingModule<T>
implements ICitModule
{
	protected final ComponentType<T> componentType;

	/*
	 * The lifetime of each entry  is roughly equivalent  to the lifetime of the
	 * associated  item stack's  component. Item components  are supposed  to be
	 * immutable, so a cache  should never  need  to be  recomputed  for a given
	 * identity.
	 */
	private final WeakHashMap<T, @Nullable ModelIdentifier> cachedModels = new WeakHashMap<>();

	public AComponentCachingModule(ComponentType<T> component){
		this.componentType = component;
	}


	@Override
	public final ModelIdentifier GetItemModel(ItemStack stack, IVariantManager models){
		T component = stack.get(this.componentType);
		if (component == null)
			return null;

		/**
		 * Do  not  use  computeIfAbsent! It would  attempt  to  recompute  null
		 * values, which are valid to cache.
		 */
		if (!cachedModels.containsKey(component))
			cachedModels.put(component, GetModelForComponent(component, models));

		return cachedModels.get(component);
	}

	public abstract ModelIdentifier GetModelForComponent(T component, IVariantManager models);
}
