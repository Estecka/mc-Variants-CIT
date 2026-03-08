package fr.estecka.variantscit.itemdata.containers;

import com.mojang.serialization.Codec;
import fr.estecka.variantscit.CodecUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public record RawDataContainer<T>(T value)
implements IDataContainer
{
	static public final Codec<RawDataContainer<Tag>> LITTERAL_CODEC = CodecUtil.NBT.xmap(RawDataContainer::new, RawDataContainer::value);

	static public <T> RawDataContainer<T> OfNullable(T value){
		if (value == null)
			return null;
		else
			return new RawDataContainer<>(value);
	}

	@Override
	public final String toString() {
		return value.toString();
	}

	public final MutableComponent toText(ChatFormatting innerFormat){
		return Component.literal("[#").withStyle(ChatFormatting.GRAY)
			.append(this.value.getClass().getSimpleName())
			.append("]{")
			.append(Component.literal(IDataContainer.printableValue(this)).withStyle(innerFormat))
			.append("}")
			;
	}
}
