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
		return CodecUtil.GetComponentNbt(value, type.codec());
	}

	@Override
	public @NotNull MutableComponent toText(ChatFormatting innerFormat) {
		return Component.literal("[@").withStyle(ChatFormatting.GRAY)
			.append(CodecUtil.ShortIdString(type))
			.append("]{")
			.append(Component.literal(IDataContainer.printableValue(this)).withStyle(innerFormat))
			.append("}")
			;
	}

	@Override
	public final String toString() {
		return this.toText().toString();
	}
}
