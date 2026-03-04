package fr.estecka.variantscit.itemdata.transforms.impl;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import fr.estecka.variantscit.itemdata.containers.IDataContainer;
import fr.estecka.variantscit.itemdata.transforms.IDataTransform;

public record NumberCompareTransform(
	double targetValue,
	short bias
)
implements IDataTransform
{
	static private final short STRICTLY_EQUAL   = 1 << 0;
	static private final short STRICTLY_SMALLER = 1 << 2;
	static private final short STRICTLY_GREATER = 1 << 3;
	static private final short SMALLER_OR_EQUAL = STRICTLY_EQUAL | STRICTLY_SMALLER;
	static private final short GREATER_OR_EQUAL = STRICTLY_EQUAL | STRICTLY_GREATER;

	static public final Codec<NumberCompareTransform> LITERAL_CODEC_EQUAL       = CodecWithBias(STRICTLY_EQUAL  );
	static public final Codec<NumberCompareTransform> LITERAL_CODEC_SMALLER     = CodecWithBias(STRICTLY_SMALLER);
	static public final Codec<NumberCompareTransform> LITERAL_CODEC_GREATER     = CodecWithBias(STRICTLY_GREATER);
	static public final Codec<NumberCompareTransform> LITERAL_CODEC_GREAT_OR_EQ = CodecWithBias(GREATER_OR_EQUAL);
	static public final Codec<NumberCompareTransform> LITERAL_CODEC_SMALL_OR_EQ = CodecWithBias(SMALLER_OR_EQUAL);

	static public final MapCodec<NumberCompareTransform> MAPCODEC_EQUAL       = LITERAL_CODEC_EQUAL      .fieldOf("equals");
	static public final MapCodec<NumberCompareTransform> MAPCODEC_SMALLER     = LITERAL_CODEC_SMALLER    .fieldOf("smaller_than");
	static public final MapCodec<NumberCompareTransform> MAPCODEC_GREATER     = LITERAL_CODEC_GREATER    .fieldOf("greater_than");
	static public final MapCodec<NumberCompareTransform> MAPCODEC_SMALL_OR_EQ = LITERAL_CODEC_SMALL_OR_EQ.fieldOf("smaller_or_equals");
	static public final MapCodec<NumberCompareTransform> MAPCODEC_GREAT_OR_EQ = LITERAL_CODEC_GREAT_OR_EQ.fieldOf("greater_or_equals");

	static private Codec<NumberCompareTransform> CodecWithBias(short bias){
		return Codec.DOUBLE.xmap(value -> new NumberCompareTransform(value, bias), NumberCompareTransform::targetValue);
	}


	@Override
	public IDataContainer LooseTypedTransform(IDataContainer inputcontainer) {
		Number input = inputcontainer.asNumber();
		if (input == null)
			return null;

		if ((Compare(input.doubleValue(), targetValue) & this.bias) != 0)
			return inputcontainer;
		else
			return null;
	}

	static private short Compare(double a, double b){
		int r = Double.compare(a, b);
		if (r == 0)
			return STRICTLY_EQUAL;
		else if (r > 0)
			return STRICTLY_GREATER;
		else
			return STRICTLY_SMALLER;
	}
}
