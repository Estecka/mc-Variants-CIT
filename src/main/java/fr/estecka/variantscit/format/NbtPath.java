package fr.estecka.variantscit.format;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import net.minecraft.nbt.CollectionTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

public final class NbtPath
{
	static public final Codec<NbtPath> CODEC = Codec.STRING.comapFlatMap(NbtPath::Parse, NbtPath::toString);
	static public final NbtPath IDENTITY = new NbtPath(new Token[0]);

	private final Token[] tokens;

	private NbtPath(Token[] tokens){
		this.tokens = tokens;
	}
	private NbtPath(List<Token> tokens){
		this.tokens = tokens.toArray(Token[]::new);
	}

	public @Nullable Tag Resolve(Tag nbt){
		for (Token tk : this.tokens){
			nbt = tk.Resolve(nbt);
			if (nbt == null) return null;
		}

		return nbt;
	}


/******************************************************************************/
/* # Parser                                                                   */
/******************************************************************************/

	private record Parsed(Token token, String remainder)
	{}

	@Deprecated
	static public DataResult<NbtPath> DotSeparatedPath(String rawPath)
	throws IllegalStateException
	{
		if (rawPath.isEmpty())
			return DataResult.success(new NbtPath(new Token[0]));
	
		String[] names = rawPath.split("\\.");
		Token[] tokens = new Token[names.length];
		for (int i=0; i<names.length; ++i)
			tokens[i] = new MapKey(names[i]);

		return DataResult.success(new NbtPath(tokens));
	}

	/**
	 * @implNote Parser functions will return NULL instead of a DataResult to
	 * signify that another parser should be used.
	 */
	static public DataResult<NbtPath> Parse(final String input){
		@SuppressWarnings("unchecked")
		final Function<String,DataResult<Parsed>>[] parsers = new Function[3];
		parsers[0] = MapKey::Next;
		parsers[1] = ArrayIndex::Next;
		parsers[2] = MapIndex::Next;

		List<Token> tokens = new ArrayList<>();
		String remainder = input;

		while (!remainder.isEmpty()){
			DataResult<Parsed> result = null;
			for (int i=0; i<parsers.length && result == null; ++i)
				result = parsers[i].apply(remainder);

			if (result == null){
				final String rem = remainder;
				result = DataResult.error(()->"Invalid character here -> "+rem);
			}

			if (result.isError()){
				return result.map(_0->new NbtPath(tokens))
				             .mapError(err->"Invalid token in path: "+input+'\n'+err)
				             ;
			}
			else {
				Parsed parsed = result.getOrThrow();
				tokens.add(parsed.token);
				remainder = parsed.remainder;
			}
		}

		return DataResult.success(new NbtPath(tokens));
	}

	@Override
	public String toString(){
		StringBuilder builder = new StringBuilder();
		for (Token tk : this.tokens)
			builder.append(tk.toString());

		return builder.toString();
	}


/******************************************************************************/
/* # Tokens                                                                   */
/******************************************************************************/

	static private interface Token {
		@Nullable Tag Resolve(Tag nbt);
	}

	static private record MapKey(String name)
	implements Token
	{
		static private final char QUOTE  = '\'';
		static private final char ESCAPE = '\\';

		@Override
		public Tag Resolve(Tag nbt){
			if (nbt instanceof CompoundTag compound)
				return compound.get(name);
			else
				return null;
		}

		@Override
		public String toString(){
			return ".'"+name+"'";
		}

		static private DataResult<Parsed> Next(String input){
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
				new MapKey(result),
				input.substring(end)
			));
		}

		static private boolean IsCharValid(char c){
			return ('A' <= c && c <= 'Z')
			    || (ResourceLocation.isAllowedInResourceLocation(c) && c != '.')
			    ;
		}
	}

	static private record ArrayIndex(int index)
	implements Token
	{
		@Override
		public Tag Resolve(Tag nbt){
			if (!(nbt instanceof CollectionTag<?> list))
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

		static private DataResult<Parsed> Next(String input){
			if (input.length() < 3 || input.charAt(0) != '[')
				return null;

			int end = input.indexOf(']');
			if (end < 0)
				return DataResult.error(()->"Empty array index here -> "+input);

			
			int index;
			try {
				index = Integer.parseUnsignedInt(input.substring(1, end));
			} catch (NumberFormatException e){
				return DataResult.error(()->"Array indx is not a number -> "+input);
			}

			return DataResult.success(new Parsed(
				new ArrayIndex(index),
				input.substring(end+1)
			));
		}
	}

	static private record MapIndex(int index)
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
			for (String k : compound.getAllKeys())
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

		static private DataResult<Parsed> Next(String input){
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
				new MapIndex(index),
				input.substring(end+1)
			));
		}

		@Override
		public String toString(){
			return "{"+index+"}";
		}
	}
}
