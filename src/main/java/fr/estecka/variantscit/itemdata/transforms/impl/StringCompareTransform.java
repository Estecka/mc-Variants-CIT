package fr.estecka.variantscit.itemdata.transforms.impl;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import fr.estecka.variantscit.itemdata.containers.IDataContainer;
import fr.estecka.variantscit.itemdata.transforms.IDataTransform;

public record StringCompareTransform(
	String targetValue
)
implements IDataTransform
{
	static public final Codec<StringCompareTransform> LITERAL_CODEC = Codec.STRING
		.xmap(StringCompareTransform::new, StringCompareTransform::targetValue)
		;

	static public final MapCodec<StringCompareTransform> MAPCODEC = LITERAL_CODEC.fieldOf("value");

	@Override
	public IDataContainer LooseTypedTransform(IDataContainer inputContainer) {
		String input = inputContainer.asString();
		if (input != null && input.equals(targetValue))
			return inputContainer;
		else
			return null;
	}
}
