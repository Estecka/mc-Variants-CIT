package fr.estecka.variantscit.itemdata.containers;

import com.mojang.serialization.Codec;
import fr.estecka.variantscit.CodecUtil;
import fr.estecka.variantscit.itemdata.functions.IDataFunction;
import net.minecraft.nbt.Tag;

public record RawDataContainer<T>(T value)
implements IDataContainer, IDataFunction
{
	static public final Codec<RawDataContainer<Tag>> LITTERAL_CODEC = CodecUtil.NBT.xmap(RawDataContainer::new, RawDataContainer::value);

	static public <T> RawDataContainer<T> OfNullable(T value){
		if (value == null)
			return null;
		else
			return new RawDataContainer<>(value);
	}

	@Override
	public IDataContainer LooseTypedTransform(IDataContainer input) {
		return this;
	}
}
