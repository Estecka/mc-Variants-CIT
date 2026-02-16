package fr.estecka.variantscit.modules.cache;

import java.util.Objects;
import org.jetbrains.annotations.Nullable;
import fr.estecka.variantscit.modules.cache.MultiPropertyCache.ICachableItemProperty;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;

public class CacheableComponent<T>
implements ICachableItemProperty
{
	protected final DataComponentType<T> componentType;

	protected CacheableComponent(DataComponentType<T> componentType){
		this.componentType = componentType;
	}

	@Override
	public final @Nullable T GetReference(ItemStack stack) {
		return stack.get(componentType);
	}

	@Override
	public final int GetPropertyHash(ItemStack stack) {
		return Objects.hashCode(GetReference(stack));
	}

	@Override
	public boolean SameSourceAs(ICachableItemProperty other) {
		return this == other
		    || other instanceof CacheableComponent othComp && this.componentType.equals(othComp.componentType)
		    ;
	}
}
