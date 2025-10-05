package fr.estecka.variantscit.format.transforms;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.apache.commons.lang3.exception.ExceptionUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.estecka.variantscit.VariantsCitMod;
import fr.estecka.variantscit.format.IStringTransform;

public record RegexTransform(Pattern pattern, String substitution, boolean matchAll, boolean validate)
implements IStringTransform
{
	static public final MapCodec<RegexTransform> MAPCODEC = RecordCodecBuilder.mapCodec(instance ->
		instance.group(
			Codec.STRING.comapFlatMap(RegexTransform::ParsePattern, Pattern::toString).fieldOf("regex").forGetter(RegexTransform::pattern),
			Codec.STRING.optionalFieldOf("substitution", "$0").forGetter(RegexTransform::substitution),
			Codec.BOOL.optionalFieldOf("matchAll", true).forGetter(RegexTransform::matchAll),
			Codec.BOOL.optionalFieldOf("validate", true).forGetter(RegexTransform::validate)
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
		if (matchAll ? match.matches() : match.find())
		try {
			return match.replaceAll(this.substitution);
		}
		catch(IndexOutOfBoundsException|IllegalArgumentException e){
			VariantsCitMod.LOGGER.error("Error in regex substitution: {}\n{}]", this.substitution, ExceptionUtils.getStackTrace(e));
		}

		return validate ? null : input;
	}
}
