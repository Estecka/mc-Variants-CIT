package fr.estecka.variantscit.itemdata.containers;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import fr.estecka.variantscit.CodecUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public record ComponentContainer<T>(
	T value,
	DataComponentType<T> type
)
implements IDataContainer
{
	static public <T> ComponentContainer<T> OfNullable(@Nullable T value, DataComponentType<T> type){
		if (value == null)
			return null;
		else
			return new ComponentContainer<>(value, type);
	}

	@Override
	public Tag asNbt() {
		var codec = type.codec();
		if (codec != null)
			return CodecUtil.GetComponentNbt(value, type.codec());
		else
			return IDataContainer.super.asNbt();
	}

	@Override
	public @NotNull MutableComponent toText() {
		return Component.empty()
			.append(Component.literal("[@"+CodecUtil.ShortIdString(type)+"]{").withStyle(ChatFormatting.GRAY))
			.append(IDataContainer.printableValue(this))
			.append(Component.literal("}").withStyle(ChatFormatting.GRAY))
			;
	}

	@Override
	public final String toString() {
		return this.toText().toString();
	}
}
