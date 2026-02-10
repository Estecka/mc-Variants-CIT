package fr.estecka.variantscit.format.properties;

import java.util.Objects;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public abstract class AMonoComponentProperty<T>
implements IStringProperty
{
	protected final DataComponentType<T> componentType;

	protected AMonoComponentProperty(DataComponentType<T> componentType){
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
