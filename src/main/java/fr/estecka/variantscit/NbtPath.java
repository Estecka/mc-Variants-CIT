package fr.estecka.variantscit;

import java.util.List;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import net.minecraft.nbt.AbstractNbtList;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

public final class NbtPath
{
	static private final Codec<String[]> STRING_ARRAY = Codec.STRING.listOf().xmap(
		list -> list.toArray(i->new String[i]),
		array -> List.of(array)
	);

	static public final Codec<String[]> CODEC = Codec.withAlternative(
		STRING_ARRAY,
		Codec.of(STRING_ARRAY, Codec.STRING.flatMap(NbtPath::DotSeparatedPath))
	);

	static public DataResult<String[]> DotSeparatedPath(String rawPath)
	throws IllegalStateException
	{
		if (rawPath.isEmpty())
			return DataResult.success(new String[0]);
	
		String[] result = rawPath.split("\\.");
		return DataResult.success(result);
	}

	static public NbtElement Resolve(NbtElement nbt, String[] path){
		for (int i=0; i<path.length; ++i)
		if  (nbt instanceof NbtCompound compound)
			nbt = compound.get(path[i]);
		else if (nbt instanceof AbstractNbtList list){
			nbt = ResolveIndex(list, path[i]);
			if (nbt == null)
				return null;
		}
		else
			return null;

		return nbt;
	}

	static private NbtElement ResolveIndex(AbstractNbtList<?> list, String rawIndex){
		int index;
		try {
			index = Integer.parseUnsignedInt(rawIndex);
		} catch (NumberFormatException e){
			return null;
		}

		if (list.size() <= index)
			return null;

		return list.get(index);
	}
}
