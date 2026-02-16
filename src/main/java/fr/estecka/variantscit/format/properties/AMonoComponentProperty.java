package fr.estecka.variantscit.format.properties;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;
import fr.estecka.variantscit.modules.cache.CacheableComponent;

public abstract class AMonoComponentProperty<T>
extends CacheableComponent<T>
implements IStringProperty
{
	protected AMonoComponentProperty(DataComponentType<T> componentType){
		super(componentType);
	}

	public abstract String GetPropertyString(T component);

	@Override
	public String GetPropertyString(ItemStack stack) {
		T component = GetReference(stack);
		return (component!=null) ? GetPropertyString(component) : null;
	}
}
