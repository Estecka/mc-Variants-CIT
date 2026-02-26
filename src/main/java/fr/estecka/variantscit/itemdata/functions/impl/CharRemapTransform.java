package fr.estecka.variantscit.itemdata.functions.impl;

import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.estecka.variantscit.CodecUtil;
import fr.estecka.variantscit.itemdata.functions.IStringTransform;
import it.unimi.dsi.fastutil.chars.Char2ObjectArrayMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectMap;

public record CharRemapTransform(
	Char2ObjectMap<@NotNull String> map
)
implements IStringTransform
{
	static public final MapCodec<CharRemapTransform> MAPCODEC = CharsetParams.MAPCODEC
		.xmap(CharsetParams::ToMap, CharsetParams::FromMap)
		.xmap(CharRemapTransform::new, CharRemapTransform::map)
		;

	@Override
	public String apply(final String original) {
		StringBuilder result = new StringBuilder();

		for (int i=0; i<original.length(); ++i)
		{
			char inChar = original.charAt(i);
			String outStr = map.get(inChar);
			// Replace / Delete
			if (outStr != null)
				result.append(outStr);
			// No-op
			else
				result.append(inChar);
		}

		return result.toString();
	}

	static private record CharsetParams(
		String source,
		String destination,
		String delete,
		Map<Character,String> map
	){
		static public final MapCodec<CharsetParams> MAPCODEC = RecordCodecBuilder.mapCodec(builder->
			builder.group(
				Codec.STRING.fieldOf("source").forGetter(CharsetParams::source),
				Codec.STRING.fieldOf("destination").forGetter(CharsetParams::destination),
				Codec.STRING.optionalFieldOf("delete", "").forGetter(CharsetParams::destination),
				Codec.unboundedMap(CodecUtil.CHAR, Codec.STRING).optionalFieldOf("map", Map.of()).forGetter(CharsetParams::map)
			)
			.apply(builder, CharsetParams::new)
		);

		static public CharsetParams FromMap(Char2ObjectMap<@NotNull String> fullMap){
			String src = "", dst = "", del = "";
			Map<Character, String> paramMap = new HashMap<>();

			for (var entry : fullMap.char2ObjectEntrySet()) {
				char   key   = entry.getCharKey();
				String value = entry.getValue();
				
				if (value.isEmpty())
					del += key;
				else if (value.length() >= 1)
					paramMap.put(key, value);
				else {
					src += key;
					dst += value;
				}
			}

			return new CharsetParams(src, dst, del, paramMap);
		}

		public Char2ObjectMap<@NotNull String> ToMap(){
			Char2ObjectMap<@NotNull String> fullMap = new Char2ObjectArrayMap<>();

			for (int i=0; i<source.length(); ++i) {
				char key = source.charAt(i);
				if (i < destination.length())
					fullMap.put(key, destination.substring(i, i+1));
				else
					fullMap.put(key, "");
			}

			for (int i=0; i<delete.length(); ++i)
				fullMap.put(delete.charAt(i), "");

			fullMap.putAll(this.map);

			return fullMap;
		}
	}
}
