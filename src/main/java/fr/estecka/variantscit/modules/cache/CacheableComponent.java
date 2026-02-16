package fr.estecka.variantscit.modules.cache;

import java.util.Objects;
import org.jetbrains.annotations.Nullable;
import fr.estecka.variantscit.modules.cache.MultiPropertyCache.ICacheablePropertySource;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;

public record CacheableComponent<T>(
	DataComponentType<T> componentType
)
implements ICacheablePropertySource
{
	@Override
	public final @Nullable T GetReference(ItemStack stack) {
		return stack.get(componentType);
	}

	@Override
	public final int GetPropertyHash(ItemStack stack) {
		return Objects.hashCode(GetReference(stack));
	}

	@Override
	public boolean equals(Object other) {
		return this == other
		    || other instanceof CacheableComponent othComp && othComp.componentType.equals(this.componentType)
		    ;
	}

	@Override
	public int hashCode() {
		return componentType.hashCode();
	}
}
