package fr.estecka.variantscit.itemdata.nbtpath;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.mojang.serialization.DataResult;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;


class MapKeyToken
implements Token
{
	static private final char QUOTE  = '\'';
	static private final char ESCAPE = '\\';

	private final @NotNull  String strictKey;
	private final @Nullable String lenientKey;

	public MapKeyToken(String mainKey){
		this.strictKey = mainKey;
		Identifier lenientKey = Identifier.tryParse("minecraft:"+mainKey);
		this.lenientKey = (lenientKey != null) ? lenientKey.toString() : null;
	}

	@Override
	public Tag Resolve(Tag nbt){
		if (nbt instanceof CompoundTag compound){
			Tag result = compound.get(strictKey);
			if (result == null && this.lenientKey != null)
				result = compound.get(lenientKey);
			return result;
		}
		else
			return null;
	}

	@Override
	public String toString(){
		return ".'"+strictKey+"'";
	}

	static public DataResult<Parsed> ParseNext(String input){
		if (input.length() < 2 || input.charAt(0) != '.')
			return null;

		String result = "";
		boolean openQuotes = false;
		boolean isEscaping = false;

		int end=1;
		if (input.charAt(1) == QUOTE){
			openQuotes = true;
			++end;
		}
		for (  ; end<input.length(); ++end)
		{
			char c = input.charAt(end);
			if (isEscaping){
				result += c;
				isEscaping = false;
			}
			else if (c == ESCAPE)
				isEscaping = true;
			else if (openQuotes && c == QUOTE){
				openQuotes = false;
				++end;
				break;
			}
			else if (openQuotes || IsCharValid(c))
				result += c;
			else
				break;
		}

		// Disallow points followed by nothing, but allow empty quotes.
		if (end <= 1){
			return DataResult.error(()->"Missing keyname after dot -> " + input
									   + "\nIf the key is intentionally empty, quote it. ( Write .'' )"
								   );
		}

		if (openQuotes)
			return DataResult.error(()->"Unclosed quote in keyname -> "+input);
		if (isEscaping)
			return DataResult.error(()->"Escape character at the end of input -> "+input);

		return DataResult.success(new Parsed(
			new MapKeyToken(result),
			input.substring(end)
		));
	}

	static private boolean IsCharValid(char c){
		return ('A' <= c && c <= 'Z')
			|| (Identifier.isAllowedInResourceLocation(c) && c != '.')
			;
	}
}
