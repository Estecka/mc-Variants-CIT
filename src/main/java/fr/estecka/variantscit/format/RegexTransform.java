package fr.estecka.variantscit.format;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record RegexTransform(Pattern pattern, String substitution)
implements IStringTransform
{
	static public final MapCodec<RegexTransform> MAPCODEC = RecordCodecBuilder.mapCodec(instance ->
		instance.group(
			Codec.STRING.comapFlatMap(RegexTransform::ParsePattern, Pattern::toString).fieldOf("regex").forGetter(RegexTransform::pattern),
			Codec.STRING.optionalFieldOf("substitution", "$0").forGetter(RegexTransform::substitution)
		).apply(instance, RegexTransform::new)
	);

	static public DataResult<Pattern> ParsePattern(String regex){
		try {
			return DataResult.success(Pattern.compile(regex));
		}
		catch (PatternSyntaxException e){
			return DataResult.error(e::toString);
		}
	}

	@Override
	public String apply(String input) {
		Matcher match = this.pattern.matcher(input);
		if (!match.matches())
			return null;
		else
			return match.replaceAll(this.substitution);
	}
}
