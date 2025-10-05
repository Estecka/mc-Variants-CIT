package fr.estecka.variantscit.format.transforms;

import java.util.Map;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.estecka.variantscit.format.IStringTransform;

public record RemapTransform(
	boolean required,
	Map<String,String> map
) 
implements IStringTransform
{
	static public final MapCodec<RemapTransform> MAPCODEC = RecordCodecBuilder.mapCodec(builder ->
		builder.group(
			Codec.BOOL.optionalFieldOf("validate", false).forGetter(RemapTransform::required),
			Codec.unboundedMap(Codec.STRING, Codec.STRING).fieldOf("names").forGetter(RemapTransform::map)
		)
		.apply(builder, RemapTransform::new)
	);

	@Override
	public String apply(final String original) {
		String result = map.get(original);
		if (result != null)
			return result;
		else
			return required ? null : original;
	}
}
