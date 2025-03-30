package fr.estecka.variantscit.format;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.jetbrains.annotations.Nullable;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import fr.estecka.variantscit.VariantsCitMod;
import net.minecraft.nbt.AbstractNbtList;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Identifier;

public final class NbtPath
{
	static public final Codec<NbtPath> CODEC = Codec.STRING.comapFlatMap(NbtPath::Parse, NbtPath::toString);

	@Deprecated
	static public final Codec<NbtPath> DOT_SEPARATED_CODEC = Codec.STRING.comapFlatMap(NbtPath::DotSeparatedPath, NbtPath::toString).validate(_0 -> {
		VariantsCitMod.LOGGER.warn("The value of `nbtPath` Uses an obsolete path format. Please add a `.` at the start of the path.");
		return DataResult.success(_0);
	});

	@Deprecated
	static public final Codec<NbtPath> NBTKEY_CODEC = Codec.STRING.xmap(s->new NbtPath(new Token[]{new MapKey(s)}), NbtPath::toString).validate(_0 -> {
		VariantsCitMod.LOGGER.warn("The parameter `nbtKey` is being deprecated. Use `nbtPath` instead.");
		return DataResult.success(_0);
	});

	private final Token[] tokens;

	private NbtPath(Token[] tokens){
		this.tokens = tokens;
	}
	private NbtPath(List<Token> tokens){
		this.tokens = tokens.toArray(Token[]::new);
	}

	public @Nullable NbtElement Resolve(NbtElement nbt){
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

	static public DataResult<NbtPath> Parse(final String input){
		final Function<String,Parsed>[] parsers = new Function[3];
		parsers[0] = MapKey::Next;
		parsers[1] = ArrayIndex::Next;
		parsers[2] = MapIndex::Next;

		List<Token> tokens = new ArrayList<>();
		String remainder = input;

		while (!remainder.isEmpty()){
			Parsed result = null;
			for (int i=0; i<parsers.length && result == null; ++i)
				result = parsers[i].apply(remainder);

			if (result == null)
				return DataResult.error(()->"Invalid token in path: "+input, new NbtPath(tokens));
			else {
				tokens.add(result.token);
				remainder = result.remainder;
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
		@Nullable NbtElement Resolve(NbtElement nbt);
	}

	static private record MapKey(String name)
	implements Token
	{
		@Override
		public NbtElement Resolve(NbtElement nbt){
			if (nbt instanceof NbtCompound compound)
				return compound.get(name);
			else
				return null;
		}

		@Override
		public String toString(){
			return "."+name;
		}

		static private Parsed Next(String input){
			if (input.length() < 2 || input.charAt(0) != '.')
				return null;

			int end;
			for (end=1; end<input.length(); ++end){
				char c = input.charAt(end);
				if (c < 'A' || 'Z' < c)
					if (!Identifier.isCharValid(c) || c == '.')
						break;
			}

			return new Parsed(
				new MapKey(input.substring(1, end)),
				input.substring(end)
			);
		}
	}

	static private record ArrayIndex(int index)
	implements Token
	{
		@Override
		public NbtElement Resolve(NbtElement nbt){
			if (!(nbt instanceof AbstractNbtList list))
				return null;

			int size = list.size();
			if (index < -size || size <= index)
				return null;

			if (index >= 0)
				return list.method_10534(index); // get
			else
				return list.method_10534(size+index); // get
		}

		@Override
		public String toString(){
			return "["+index+"]";
		}

		static private Parsed Next(String input){
			if (input.length() < 3 || input.charAt(0) != '[')
				return null;

			int end = input.indexOf(']');
			if (end < 0)
				return null;

			
			int index;
			try {
				index = Integer.parseUnsignedInt(input.substring(1, end));
			} catch (NumberFormatException e){
				return null;
			}

			return new Parsed(
				new ArrayIndex(index),
				input.substring(end+1)
			);
		}
	}

	static private record MapIndex(int index)
	implements Token
	{
		@Override
		public NbtElement Resolve(NbtElement nbt){
			if (!(nbt instanceof NbtCompound compound))
				return null;

			int size = compound.getSize();
			if (index < -size || size <= index)
				return null;

			int i = index;
			if (i < 0) i += size;

			String key = null;
			for (String k : compound.getKeys())
			if (i-- <= 0){
				key = k;
				break;
			}

			if (key == null)
				return null;

			NbtCompound result = new NbtCompound();
			result.putString("key", key);
			result.put("value", compound.get(key));
			return result;
		}

		static private Parsed Next(String input){
			if (input.length() < 3 || input.charAt(0) != '{')
				return null;

			int end = input.indexOf('}');
			if (end < 0)
				return null;

			
			int index;
			try {
				index = Integer.parseUnsignedInt(input.substring(1, end));
			} catch (NumberFormatException e){
				return null;
			}

			return new Parsed(
				new MapIndex(index),
				input.substring(end+1)
			);
		}

		@Override
		public String toString(){
			return "{"+index+"}";
		}
	}
}
