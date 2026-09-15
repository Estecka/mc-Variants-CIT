package fr.estecka.variantscit.itemdata.nbtpath;

import com.mojang.serialization.DataResult;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;


record MapIndexToken(int index)
implements Token
{
	@Override
	public Tag Resolve(Tag nbt){
		if (!(nbt instanceof CompoundTag compound))
			return null;

		int size = compound.size();
		if (index < -size || size <= index)
			return null;

		int i = index;
		if (i < 0) i += size;

		String key = null;
		for (String k : compound.keySet())
		if (i-- <= 0){
			key = k;
			break;
		}

		if (key == null)
			return null;

		CompoundTag result = new CompoundTag();
		result.putString("key", key);
		result.put("value", compound.get(key));
		return result;
	}

	static public DataResult<Parsed> ParseNext(String input){
		if (input.length() < 3 || input.charAt(0) != '{')
			return null;

		int end = input.indexOf('}');
		if (end < 0)
			return DataResult.error(()->"Empty map index here -> "+input);

		
		int index;
		try {
			index = Integer.parseUnsignedInt(input.substring(1, end));
		} catch (NumberFormatException e){
			return DataResult.error(()->"Map index is not a number -> "+input);
		}

		return DataResult.success(new Parsed(
			new MapIndexToken(index),
			input.substring(end+1)
		));
	}

	@Override
	public String toString(){
		return "{"+index+"}";
	}
}
