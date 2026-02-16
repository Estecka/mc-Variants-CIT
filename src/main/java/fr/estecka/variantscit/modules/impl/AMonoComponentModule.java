package fr.estecka.variantscit.modules.impl;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import java.util.Collection;
import fr.estecka.variantscit.modules.cache.CacheableComponent;
import fr.estecka.variantscit.modules.cache.MultiPropertyCache.ICacheablePropertySource;
import fr.estecka.variantscit.modules.libraries.IVariantCitModule;
import fr.estecka.variantscit.modules.libraries.IVariantLibrary;


abstract class AMonoComponentModule<T>
implements IVariantCitModule
{
	protected final DataComponentType<T> componentType;

	public AMonoComponentModule(DataComponentType<T> component){
		this.componentType = component;
	}

	@Override
	public Collection<ICacheablePropertySource> GetCacheSources() {
		return CacheableComponent.SourcesOf(componentType);
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
