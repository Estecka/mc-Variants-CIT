package fr.estecka.variantscit.modules.cache;

import java.util.stream.Stream;
import org.jetbrains.annotations.Nullable;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;

public record ComponentCacheKey<T>(
	DataComponentType<T> componentType
)
implements ICacheKey
{
	static public CacheKeySet KeysOf(DataComponentType<?>... types){
		return CacheKeySet.Of(Stream.of(types).map(ComponentCacheKey::new));
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

	@Override
	public final String toString() {
		// ResourceLocation id = BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(this.componentType);
		// String name = (id == null) ? componentType.toString() : id.toString();
		return "@"+componentType.toString();
	}
}
