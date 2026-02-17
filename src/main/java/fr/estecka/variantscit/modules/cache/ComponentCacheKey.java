package fr.estecka.variantscit.modules.cache;

import java.util.Collection;
import java.util.stream.Stream;
import org.jetbrains.annotations.Nullable;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;

public record ComponentCacheKey<T>(
	DataComponentType<T> componentType
)
implements ICacheKey
{
	static public Collection<ICacheKey> KeysOf(DataComponentType<?>... types){
		return Stream.of(types)
			.distinct()
			.<ICacheKey>map(ComponentCacheKey::new)
			.toList()
			;
	}

	@Override
	public final @Nullable T GetReference(ItemStack stack) {
		return stack.get(componentType);
	}

	@Override
	public boolean equals(Object other) {
		return this == other
		    || other instanceof ComponentCacheKey othComp && othComp.componentType.equals(this.componentType)
		    ;
	}

	@Override
	public int hashCode() {
		return componentType.hashCode();
	}
}
