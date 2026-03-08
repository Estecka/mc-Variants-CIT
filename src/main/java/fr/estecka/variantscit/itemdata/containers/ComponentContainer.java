package fr.estecka.variantscit.itemdata.containers;

import org.jetbrains.annotations.NotNull;
import fr.estecka.variantscit.CodecUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

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

	@Override
	public @NotNull MutableComponent toText(ChatFormatting innerFormat) {
		String idString = "???";
		ResourceLocation id = BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(type);
		if (id != null)
			idString = CodecUtil.ShortIdString(id);

		return Component.literal("[@").withStyle(ChatFormatting.GRAY)
			.append(idString)
			.append("]{")
			.append(Component.literal(IDataContainer.printableValue(this)).withStyle(innerFormat))
			.append("}")
			;
	}
}
