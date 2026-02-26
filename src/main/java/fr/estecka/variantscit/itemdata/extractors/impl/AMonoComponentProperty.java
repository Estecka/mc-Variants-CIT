package fr.estecka.variantscit.itemdata.extractors.impl;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;
import fr.estecka.variantscit.itemdata.containers.IDataContainer;
import fr.estecka.variantscit.itemdata.containers.RawDataContainer;
import fr.estecka.variantscit.itemdata.extractors.IDataExtractor;
import fr.estecka.variantscit.modules.cache.ComponentCacheKey;

public abstract class AMonoComponentProperty<I,O>
implements IDataExtractor
{
	protected final ComponentCacheKey<I> source;

	protected AMonoComponentProperty(DataComponentType<I> componentType){
		this.source = new ComponentCacheKey<>(componentType);
	}

	public abstract O GetPropertyString(I component);

	@Override
	public IDataContainer Extract(ItemStack stack) {
		I component = source.GetReference(stack);
		return RawDataContainer.OfNullable((component!=null) ? GetPropertyString(component) : null);
	}

	@Override
	public ComponentCacheKey<I> GetCacheKey() {
		return source;
	}
}
