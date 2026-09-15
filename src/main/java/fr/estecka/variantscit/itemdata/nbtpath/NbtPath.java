package fr.estecka.variantscit.itemdata.nbtpath;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.Nullable;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import fr.estecka.variantscit.itemdata.containers.IDataContainer;
import fr.estecka.variantscit.itemdata.containers.RawDataContainer;
import fr.estecka.variantscit.itemdata.transforms.IDataTransform;


public final class NbtPath
implements IDataTransform
{
	static public final Codec<NbtPath> CODEC = Codec.STRING.comapFlatMap(NbtPath::Parse, NbtPath::toString);
	static public final MapCodec<NbtPath> MAPCODEC = CODEC.fieldOf("nbtPath");
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

	@Override
	public IDataContainer LooseTypedTransform(IDataContainer input) {
		Tag nbt = input.asNbt();
		if (nbt != null)
			return RawDataContainer.<Tag>OfNullable(this.Resolve(nbt));
		else
			return null;
	}


/******************************************************************************/
/* # Parser                                                                   */
/******************************************************************************/

	@Deprecated
	static public DataResult<NbtPath> DotSeparatedPath(String rawPath)
	throws IllegalStateException
	{
		if (rawPath.isEmpty())
			return DataResult.success(new NbtPath(new Token[0]));
	
		String[] names = rawPath.split("\\.");
		Token[] tokens = new Token[names.length];
		for (int i=0; i<names.length; ++i)
			tokens[i] = new MapKeyToken(names[i]);

		return DataResult.success(new NbtPath(tokens));
	}

	/**
	 * @implNote Parser functions will return NULL instead of a DataResult to
	 * signify that another parser should be used.
	 */
	static public DataResult<NbtPath> Parse(final String input){
		@SuppressWarnings("unchecked")
		final Function<String,DataResult<Parsed>>[] parsers = new Function[3];
		parsers[0] = MapKeyToken::ParseNext;
		parsers[1] = ArrayIndexToken::ParseNext;
		parsers[2] = MapIndexToken::ParseNext;

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
				tokens.add(parsed.token());
				remainder = parsed.remainder();
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
}
