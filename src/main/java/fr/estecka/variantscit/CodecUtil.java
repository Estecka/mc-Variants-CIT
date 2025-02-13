package fr.estecka.variantscit;

import java.util.List;
import com.mojang.serialization.Codec;

public class CodecUtil
{
	static public <T> Codec<List<T>> OneOrMany(Codec<T> original){
		var listCodec = original.listOf();
		return Codec.withAlternative(
			listCodec,
			Codec.of(listCodec, original.map(t->List.of(t)))
		);
	}
}
