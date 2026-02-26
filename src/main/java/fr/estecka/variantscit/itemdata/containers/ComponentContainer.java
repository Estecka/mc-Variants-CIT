package fr.estecka.variantscit.itemdata.containers;

import fr.estecka.variantscit.CodecUtil;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.Tag;

public record ComponentContainer<T>(
	T value,
	DataComponentType<T> type
)
implements IDataContainer
{
	@Override
	public Tag asNbt() {
		return CodecUtil.GetComponentNbt(value, type.codec());
	}
}
