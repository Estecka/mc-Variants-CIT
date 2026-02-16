package fr.estecka.variantscit.format.properties;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;
import fr.estecka.variantscit.modules.cache.CacheableComponent;

public abstract class AMonoComponentProperty<T>
implements IStringProperty
{
	protected final CacheableComponent<T> source;

	protected AMonoComponentProperty(DataComponentType<T> componentType){
		this.source = new CacheableComponent<>(componentType);
	}

	public abstract String GetPropertyString(T component);

	@Override
	public String GetPropertyString(ItemStack stack) {
		T component = source.GetReference(stack);
		return (component!=null) ? GetPropertyString(component) : null;
	}

	@Override
	public CacheableComponent<T> GetSource() {
		return source;
	}
}
