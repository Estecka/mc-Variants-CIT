package fr.estecka.variantscit.itemdata.transforms.impl;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.exception.ExceptionUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.estecka.variantscit.CodecUtil;
import fr.estecka.variantscit.VariantsCitMod;
import fr.estecka.variantscit.itemdata.transforms.IStringTransform;

public record RegexTransform(
	Pattern pattern,
	String substitution,
	boolean firstMatchingLine,
	boolean matchAll,
	@Deprecated boolean validate
)
implements IStringTransform
{
	static private final Codec<Boolean> MULTILINE_CODEC = CodecUtil.Enum(Codec.STRING, Map.of(
		"regex_default", false,
		"first_match_only", true
	));

	static public final MapCodec<RegexTransform> MAPCODEC = RecordCodecBuilder.mapCodec(instance ->
		instance.group(
			CodecUtil.REGEX.fieldOf("regex").forGetter(RegexTransform::pattern),
			Codec.STRING.optionalFieldOf("substitution", "$0").forGetter(RegexTransform::substitution),
			MULTILINE_CODEC.optionalFieldOf("multiline_handling", true).forGetter(RegexTransform::firstMatchingLine),
			Codec.BOOL.optionalFieldOf("matchAll", true).forGetter(RegexTransform::matchAll),
			Codec.BOOL.optionalFieldOf("validate", true).forGetter(RegexTransform::validate)
		).apply(instance, RegexTransform::new)
	);

	@Override
	public String apply(String input) {
		String result = this.firstMatchingLine ?
			this.FirstMatchingLine(input) :
			this.ApplyRegex(input)
			;

		return (result != null) ? result :
		       (validate) ? null :
		       input;
	}

	private String ApplyRegex(String input){
		Matcher match = this.pattern.matcher(input);
		if (matchAll ? match.matches() : match.find())
		try {
			return match.replaceAll(this.substitution);
		}
		catch(IndexOutOfBoundsException|IllegalArgumentException e){
			VariantsCitMod.LOGGER.error("Error in regex substitution: {}\n{}]", this.substitution, ExceptionUtils.getStackTrace(e));
		}

		return null;
	}

	private String FirstMatchingLine(String multiline){
		int start, end;
		for (start=0; start<multiline.length(); start=end+1)
		{
			end = multiline.indexOf('\n', start);
			if (end < 0)
				end = multiline.length();

			String result = this.ApplyRegex(multiline.substring(start, end));
			if (result != null)
				return result;
		}

		return null;
	}
}
