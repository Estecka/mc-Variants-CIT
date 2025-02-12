package fr.estecka.variantscit.nbt;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.util.dynamic.Codecs;

public class Word
{
	static public final Codec<String> CODEC = Codecs.NON_EMPTY_STRING.validate(Word::Validate);

	static public final DataResult<String> Validate(String input){
		if (IsStringValid(input))
			return DataResult.success(input);
		else
			return DataResult.error(()->"Invalid character in string: "+input);
	}

	static public final boolean IsStringValid(String input){
		for (int i=0; i<input.length(); ++i)
			if (!IsCharValid(input.charAt(i)))
				return false;

		return true;
	}

	static public final boolean IsCharValid(char c){
		return 'a' <= c && c <= 'z';
	}
}
