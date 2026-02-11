package fr.estecka.variantscit.format.transforms;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.exception.ExceptionUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.estecka.variantscit.CodecUtil;
import fr.estecka.variantscit.VariantsCitMod;
import fr.estecka.variantscit.format.IStringTransform;

public record RegexTransform(Pattern pattern, String substitution, boolean matchAll, @Deprecated boolean validate)
implements IStringTransform
{
	static public final MapCodec<RegexTransform> MAPCODEC = RecordCodecBuilder.mapCodec(instance ->
		instance.group(
			CodecUtil.REGEX.fieldOf("regex").forGetter(RegexTransform::pattern),
			Codec.STRING.optionalFieldOf("substitution", "$0").forGetter(RegexTransform::substitution),
			Codec.BOOL.optionalFieldOf("matchAll", true).forGetter(RegexTransform::matchAll),
			Codec.BOOL.optionalFieldOf("validate", true).forGetter(RegexTransform::validate)
		).apply(instance, RegexTransform::new)
	);

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
