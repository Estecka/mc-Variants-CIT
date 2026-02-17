package fr.estecka.variantscit.format.properties;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;
import fr.estecka.variantscit.modules.cache.ComponentCacheKey;

public abstract class AMonoComponentProperty<T>
implements IStringProperty
{
	protected final ComponentCacheKey<T> source;

	protected AMonoComponentProperty(DataComponentType<T> componentType){
		this.source = new ComponentCacheKey<>(componentType);
	}

	public abstract String GetPropertyString(T component);

	@Override
	public String GetPropertyString(ItemStack stack) {
		T component = source.GetReference(stack);
		return (component!=null) ? GetPropertyString(component) : null;
	}

	@Override
	public ComponentCacheKey<T> GetCacheKey() {
		return source;
	}
}
