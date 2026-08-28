package fr.estecka.variantscit.modules.cache;

import java.util.Collection;
import java.util.stream.Stream;
import org.jetbrains.annotations.Nullable;
import fr.estecka.variantscit.util.CodecUtil;
import fr.estecka.variantscit.itemdata.containers.ComponentContainer;
import fr.estecka.variantscit.itemdata.containers.IDataContainer;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;

public record ComponentCacheKey<T>(
	DataComponentType<T> componentType
)
implements ICacheKey
{
	@SuppressWarnings("unchecked")
	static public CacheKeySet KeysOf(DataComponentType<?>... types){
		return CacheKeySet.Of(Stream.of(types).map(ComponentCacheKey::new));
	}

	@SuppressWarnings("unchecked")
	static public CacheKeySet KeysOf(Collection<DataComponentType<?>> types){
		return CacheKeySet.Of(types.stream().map(ComponentCacheKey::new));
	}

	@Override
	public IDataContainer Extract(ItemStack stack) {
		return ComponentContainer.OfNullable(this.GetReference(stack), componentType);
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
		return "@"+CodecUtil.ShortIdString(componentType);
	}
}
