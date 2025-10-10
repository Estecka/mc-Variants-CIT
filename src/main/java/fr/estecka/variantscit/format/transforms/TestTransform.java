package fr.estecka.variantscit.format.transforms;

import com.mojang.serialization.MapCodec;
import fr.estecka.variantscit.format.IStringTransform;

public record TestTransform(IStringTransform subTransform)
implements IStringTransform
{
	static public final MapCodec<TestTransform> MAPCODEC = SuccessiveTransform.CODEC
		.fieldOf("subTransform")
		.xmap(TestTransform::new, TestTransform::subTransform)
		;

	@Override
	public String apply(final String original) {
		String r = subTransform.apply(original);
		return (r == null) ? null : original;
	}
}
