package fr.estecka.variantscit.itemdata.functions.impl;

import java.util.Map;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import fr.estecka.variantscit.itemdata.functions.IStringTransform;

public record RemapTransform(Map<String,String> map) 
implements IStringTransform
{
	static public final MapCodec<RemapTransform> MAPCODEC = Codec.unboundedMap(Codec.STRING, Codec.STRING)
		.fieldOf("map")
		.xmap(RemapTransform::new, RemapTransform::map)
		;

	@Override
	public String apply(final String original) {
		return map.get(original);
	}
}
