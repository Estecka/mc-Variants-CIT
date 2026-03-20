package fr.estecka.variantscit.modules.impl;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import fr.estecka.variantscit.modules.cache.CacheKeySet;
import fr.estecka.variantscit.modules.cache.ComponentCacheKey;
import fr.estecka.variantscit.modules.cache.ECachePolicy;
import fr.estecka.variantscit.modules.libraries.IVariantCitModule;
import fr.estecka.variantscit.modules.libraries.IVariantLibrary;


abstract class AMonoComponentModule<T>
implements IVariantCitModule
{
	protected final DataComponentType<T> componentType;
	private final ECachePolicy cachePolicy;

	public AMonoComponentModule(DataComponentType<T> component, ECachePolicy cachePolicy){
		this.componentType = component;
		this.cachePolicy = cachePolicy;
	}

	@Override
	public CacheKeySet GetCacheKeys() {
		return ComponentCacheKey.KeysOf(componentType);
	}

	@Override
	public final ECachePolicy GetCachePolicy() {
		return cachePolicy;
	}

	@Override
	public final ResourceLocation GetItemModel(ItemStack stack, IVariantLibrary models){
		T component = stack.get(this.componentType);
		if (component == null)
			return null;
		else
			return this.GetModelForComponent(component, models);
	}

	public abstract ResourceLocation GetModelForComponent(T component, IVariantLibrary models);
}
