package fr.estecka.variantscit.format.transforms;

import org.jetbrains.annotations.Nullable;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.estecka.variantscit.format.IStringTransform;
import it.unimi.dsi.fastutil.chars.Char2ObjectArrayMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectMap;

public record CharRemapTransform(
	Char2ObjectMap<@Nullable Character> map
)
implements IStringTransform
{
	static public final MapCodec<CharRemapTransform> MAPCODEC = StringCharmap.MAPCODEC
		.xmap(StringCharmap::ToMap, StringCharmap::FromMap)
		.xmap(CharRemapTransform::new, CharRemapTransform::map)
		;

	@Override
	public String apply(final String original) {
		StringBuilder result = new StringBuilder();

		for (int i=0; i<original.length(); ++i)
		{
			char inChar = original.charAt(i);
			Character outChar = map.get(inChar);
			// Replace
			if (outChar != null)
				result.append(outChar);
			// No-op
			else if (!map.containsKey(inChar))
				result.append(inChar);
			// else 
				// delete
		}

		return result.toString();
	}

	static private record StringCharmap(String source, String destination)
	{
		static public final MapCodec<StringCharmap> MAPCODEC = RecordCodecBuilder.mapCodec(builder->
			builder.group(
				Codec.STRING.fieldOf("source").forGetter(StringCharmap::source),
				Codec.STRING.fieldOf("destination").forGetter(StringCharmap::destination)
			)
			.apply(builder, StringCharmap::new)
		);

		static public StringCharmap FromMap(Char2ObjectMap<@Nullable Character> map){
			StringBuilder src = new StringBuilder();
			StringBuilder dst = new StringBuilder();

			for (var entry : map.char2ObjectEntrySet()) {
				char key = entry.getCharKey();
				Character value = entry.getValue();
				if (value != null){
					src.append(key);
					dst.append(value);
				}
			}

			// Deletable characters must be at the end.
			for (var entry : map.char2ObjectEntrySet()) {
				if (entry.getValue() == null)
					src.append(entry.getCharKey());
			}

			return new StringCharmap(src.toString(), dst.toString());
		}

		public Char2ObjectMap<@Nullable Character> ToMap(){
			Char2ObjectMap<@Nullable Character> map = new Char2ObjectArrayMap<>();

			for (int i=0; i<source.length(); ++i)
			{
				char key = source.charAt(i);
				if (i < destination.length())
					map.put(key, (Character)destination.charAt(i));
				else
					map.put(key, null);
			}

			return map;
		}
	}
}
