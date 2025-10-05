package fr.estecka.variantscit.format.transforms;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.estecka.variantscit.VCitRegistries;
import fr.estecka.variantscit.format.IStringTransform;

public record OptionalTransform(IStringTransform inner)
implements IStringTransform
{

	static public final MapCodec<IStringTransform> MAPCODEC = RecordCodecBuilder.mapCodec(builder->
		builder.group(
			Codec.BOOL.optionalFieldOf("optional", false).forGetter(OptionalTransform::IsOptional),
			VCitRegistries.TRANSFORMS.mapCodec.forGetter(OptionalTransform::Unwrap)
		)
		.apply(builder, OptionalTransform::Wrap)
	);

	static private IStringTransform Wrap(boolean optional, IStringTransform inner){
		if (optional)
			return new OptionalTransform(inner);
		else
			return inner;
	}

	static private IStringTransform Unwrap(IStringTransform transform){
		if (transform instanceof OptionalTransform opt)
			return opt.inner;
		else
			return transform;
	}

	static private boolean IsOptional(IStringTransform transform){
		return transform instanceof OptionalTransform;
	}

	@Override
	public String apply(String t) {
		// TODO Auto-generated method stub
		return null;
	}
}
