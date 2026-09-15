package fr.estecka.variantscit.itemdata.nbtpath;

import com.mojang.serialization.DataResult;
import net.minecraft.nbt.CollectionTag;
import net.minecraft.nbt.Tag;


record ArrayIndexToken(int index)
implements Token
{
	@Override
	public Tag Resolve(Tag nbt){
		if (!(nbt instanceof CollectionTag list))
			return null;

		int size = list.size();
		if (index < -size || size <= index)
			return null;

		if (index >= 0)
			return list.get(index);
		else
			return list.get(size+index);
	}

	@Override
	public String toString(){
		return "["+index+"]";
	}

	static public DataResult<Parsed> ParseNext(String input){
		if (input.length() < 3 || input.charAt(0) != '[')
			return null;

		int end = input.indexOf(']');
		if (end < 0)
			return DataResult.error(()->"Empty array index here -> "+input);

		
		int index;
		try {
			index = Integer.parseUnsignedInt(input.substring(1, end));
		} catch (NumberFormatException e){
			return DataResult.error(()->"Array index is not a number -> "+input);
		}

		return DataResult.success(new Parsed(
			new ArrayIndexToken(index),
			input.substring(end+1)
		));
	}
}
