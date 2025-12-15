package fr.estecka.variantscit.assetgen;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.exception.ExceptionUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.estecka.variantscit.CodecUtil;
import fr.estecka.variantscit.VariantsCitMod;
import net.minecraft.util.Identifier;

public record TemplatedAssetGenerator(
	EAssetGenPass pass,
	Pattern inputRegex,
	// String radicalSubst,
	String outputSubst,
	FilledTemplate template
)
implements IAssetGenerator
{
	static public final MapCodec<TemplatedAssetGenerator> MAPCODEC = RecordCodecBuilder.mapCodec(builder->
		builder.group(
			EAssetGenPass.CODEC.fieldOf("pass").forGetter(TemplatedAssetGenerator::pass),
			CodecUtil.REGEX.optionalFieldOf("input", Pattern.compile(".*")).forGetter(TemplatedAssetGenerator::inputRegex),
			Codec.STRING.optionalFieldOf("output", "$0").forGetter(TemplatedAssetGenerator::outputSubst),
			FilledTemplate.MAPCODEC.forGetter(TemplatedAssetGenerator::template)
		)
		.apply(builder, TemplatedAssetGenerator::new)
	);

	public TemplatedAssetGenerator(
		EAssetGenPass pass,
		Pattern inputRegex,
		FilledTemplate template
	){
		this(pass, inputRegex, "$0", template);
	}

	@Override
	public IAssetGenerator.Result AcceptAsset(EAssetGenPass pass, Identifier inputId) {
		IAssetGenerator.Result result = new Result();
		Matcher inputMatcher = inputRegex.matcher(inputId.getPath());
		if (pass != this.pass || !inputMatcher.matches())
			return result;

		// inputMatcher.replaceAll(radicalSubst);

		// Identifier radicalId = Substitute(inputMatcher, radicalSubst).orElse(assetId);

		Optional<String> outPath = SubstitutePath(inputMatcher, outputSubst);
		if (outPath.isPresent()){
			Identifier outputId = inputId.withPath(outPath.get());
			result.putIfAbsent(
				outputId,
				template.Backfilled(Map.of("NAMESPACE", inputId.getNamespace()))
				        .Backfilled(FilledTemplate.IdVariables("INPUT",  pass.input .GetVanillaId(inputId )))
				        .Backfilled(FilledTemplate.IdVariables("OUTPUT", pass.output.GetVanillaId(outputId)))
			);
		}

		return result;
	}

	static private Optional<String> SubstitutePath(Matcher matcher, String substitution){
		String stringResult;
		try {
			stringResult = matcher.replaceAll(substitution);
		}
		catch(IndexOutOfBoundsException|IllegalArgumentException e){
			VariantsCitMod.LOGGER.error(
				"Error in regex substitution:\n- Regex: {}\n- Substitution: {}\n{}",
				matcher.pattern().pattern(),
				substitution,
				ExceptionUtils.getStackTrace(e)
			);
			return Optional.empty();
		}
		if (!Identifier.isPathValid(stringResult)){
			VariantsCitMod.LOGGER.error(
				"Asset Generator resulted in invalid identifier path: {}:{}\n- Regex: {}\n- Substitution: {}",
				stringResult,
				matcher.pattern().pattern(),
				substitution
			);
			return Optional.empty();
		}

		return Optional.of(stringResult);
	}
}
