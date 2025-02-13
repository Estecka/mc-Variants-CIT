package fr.estecka.variantscit.nbt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;

public class Substitution
{
	@FunctionalInterface
	static private interface Token {
		String Substitute(Map<String,String> variables);
	}

	static public final Codec<Substitution> CODEC = Codec.STRING.comapFlatMap(Substitution::Parse, Substitution::toString);
	static public final Codec<String> VARNAME_CODEC = Codecs.NON_EMPTY_STRING.validate(Substitution::ValidateVarname);

	private Token[] tokens;

	private Substitution(List<Token> tokens){
		this.tokens = tokens.toArray(i->new Token[i]);
	}

	public String Substitute(Map<String,String> variables){
		StringBuilder builder = new StringBuilder();
		for (Token t : this.tokens)
			builder.append(t.Substitute(variables));
		return builder.toString();
	}

	@Override
	public String toString(){
		StringBuilder builder = new StringBuilder();
		for (Token t : this.tokens)
			builder.append(t.toString());
		return builder.toString();
	}


/******************************************************************************/
/* # Tokens                                                                   */
/******************************************************************************/

	static private record Literal(String value)
	implements Token
	{
		@Override public String Substitute(Map<String,String> variables){ return value; }
		@Override public String toString(){ return value; }
	}

	static private record Variable(String name)
	implements Token
	{
		@Override public String Substitute(Map<String,String> variables){ return variables.getOrDefault(name, ""); }
		@Override public String toString(){ return "${"+name+"}"; }
	}


/******************************************************************************/
/* # Parser                                                                   */
/******************************************************************************/

	static private record Parsed<T extends Token>(T token, String remainder)
	{}

	static public DataResult<Substitution> Parse(String remainder)
	{
		List<Token> result = new ArrayList<>();

		while (!remainder.isEmpty()) {
			Parsed<?> parsed;
			try {
				if (!remainder.startsWith("$"))
					parsed = NextLiteral(remainder);
				else
					parsed = NextVariable(remainder);
			} catch (IllegalArgumentException e) {
				return DataResult.error(e::getMessage);
			}

			result.add(parsed.token);
			remainder = parsed.remainder;
		}

	
		return DataResult.success(new Substitution(result));
	}

	static private Parsed<Literal> NextLiteral(String input)
	throws IllegalArgumentException
	{
		int end = input.indexOf("$");
		if (end < 0)
			end = input.length();

		String literal = input.substring(0, end);

		if (Identifier.isPathValid(literal))
			return new Parsed<>( new Literal(literal), input.substring(end) );
		else {
			throw new IllegalArgumentException("Invalid character in path");
		}
	}

	static private Parsed<Variable> NextVariable(String input)
	throws IllegalArgumentException
	{
		int end;
		String name;
		
		if (input.startsWith("${")
		&& (end = input.indexOf("}")) > 2
		&& IsVarnameValid( name = input.substring(2, end-1) )
		){
			return new Parsed<>(
				new Variable(name),
				input.substring(end)
			);
		}
		else
			throw new IllegalArgumentException("Invalid variable format");
	}

	static public final DataResult<String> ValidateVarname(String input){
		if (IsVarnameValid(input))
			return DataResult.success(input);
		else
			return DataResult.error(()->"Invalid character in string: "+input);
	}

	static public final boolean IsVarnameValid(String input){
		for (int i=0; i<input.length(); ++i)
			if (!IsVarcharValid(input.charAt(i)))
				return false;

		return true;
	}

	static public final boolean IsVarcharValid(char c){
		return 'a' <= c && c <= 'z';
	}
}
