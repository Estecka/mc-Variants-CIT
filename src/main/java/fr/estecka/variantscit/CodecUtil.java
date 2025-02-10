package fr.estecka.variantscit;

import java.util.List;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

public final class CodecUtil
{
	static public final Codec<String[]> STRING_ARRAY = Codec.STRING.listOf().xmap(
		list -> list.toArray(i->new String[i]),
		array -> List.of(array)
	);

	static public final Codec<String[]> NBTPATH_CODEC = Codec.withAlternative(
		STRING_ARRAY,
		Codec.of(STRING_ARRAY, Codec.STRING.flatMap(CodecUtil::DotSeparatedPath))
	);

	static public DataResult<String[]> DotSeparatedPath(String rawPath)
	throws IllegalStateException
	{
		if (rawPath.isEmpty())
			return DataResult.success(new String[0]);
	
		String[] result = rawPath.split("\\.");
		return DataResult.success(result);
	}
}
