package fr.estecka.variantscit.format.properties;

import java.util.Objects;
import org.jetbrains.annotations.Nullable;
import net.minecraft.component.ComponentType;
import net.minecraft.item.ItemStack;

public abstract class AMonoComponentProperty<T>
implements IStringProperty
{
	protected final ComponentType<T> componentType;

	protected AMonoComponentProperty(ComponentType<T> componentType){
		this.componentType = componentType;
	}

	public abstract String GetPropertyString(T component);

	@Override
	public final @Nullable T GetReference(ItemStack stack) {
		return stack.get(componentType);
	}

	@Override
	public final int GetPropertyHash(ItemStack stack) {
		return Objects.hashCode(GetReference(stack));
	}

	@Override
	public final String GetPropertyString(ItemStack stack) {
		T component = GetReference(stack);
		return (component!=null) ? GetPropertyString(component) : null;
	}
}
