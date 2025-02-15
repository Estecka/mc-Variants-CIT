package fr.estecka.variantscit;

import java.util.List;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

public class CodecUtil
{
	static public <T> Codec<List<T>> OneOrMany(Codec<T> original){
		var listCodec = original.listOf();
		return Codec.withAlternative(
			listCodec,
			Codec.of(listCodec, original.map(t->List.of(t)))
		);
	}

	static public <T> MapCodec<T> MapWithAlternative(MapCodec<T> primary, MapCodec<T> alternative){
		return MapCodec.assumeMapUnsafe(Codec.withAlternative(primary.codec(), alternative.codec()));
	}
}
