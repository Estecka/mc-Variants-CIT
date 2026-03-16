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
	static public final Codec<RawDataContainer<Tag>> LITTERAL_CODEC = CodecUtil.NBT_ELEMENT.xmap(RawDataContainer::new, RawDataContainer::value);

	static public <T> RawDataContainer<T> OfNullable(T value){
		if (value == null)
			return null;
		else
			return new RawDataContainer<>(value);
	}

	@Override
	public final MutableComponent toText(){
		return	Component.empty()
			.append(Component.literal("[#"+this.value.getClass().getSimpleName()+"]{").withStyle(ChatFormatting.GRAY))
			.append(IDataContainer.printableValue(this))
			.append(Component.literal("}").withStyle(ChatFormatting.GRAY))
			;
	}

	@Override
	public final String toString() {
		return this.toText().getString();
	}
}
