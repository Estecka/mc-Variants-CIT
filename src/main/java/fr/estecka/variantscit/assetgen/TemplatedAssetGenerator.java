package fr.estecka.variantscit.assetgen;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.exception.ExceptionUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.estecka.variantscit.CodecUtil;
import fr.estecka.variantscit.VariantsCitMod;

public record TemplatedAssetGenerator(
	EAssetGenPass pass,
	Pattern inputRegex,
	String outputSubst,
	Optional<String> radicalSubst,
	FilledTemplate template
)
implements IAssetGenerator
{
	static public final MapCodec<TemplatedAssetGenerator> MAPCODEC = RecordCodecBuilder.mapCodec(builder->
		builder.group(
			EAssetGenPass.CODEC.fieldOf("pass").forGetter(TemplatedAssetGenerator::pass),
			CodecUtil.REGEX.optionalFieldOf("inputPath", Pattern.compile("^.*$")).forGetter(TemplatedAssetGenerator::inputRegex),
			Codec.STRING.optionalFieldOf("outputPath", "$0").forGetter(TemplatedAssetGenerator::outputSubst),
			Codec.STRING.optionalFieldOf("radicalPath").forGetter(TemplatedAssetGenerator::radicalSubst),
			FilledTemplate.MAPCODEC.forGetter(TemplatedAssetGenerator::template)
		)
		.apply(builder, TemplatedAssetGenerator::new)
	);

	public TemplatedAssetGenerator(
		EAssetGenPass pass,
		Pattern inputRegex,
		FilledTemplate template
	){
		this(pass, inputRegex, "$0", Optional.of("$0"), template);
	}

	@Override
	public IAssetGenerator.Result AcceptAsset(EAssetGenPass pass, ResourceLocation inputId) {
		IAssetGenerator.Result result = new Result();
		Matcher inputMatcher = inputRegex.matcher(inputId.getPath());
		if (pass != this.pass || !inputMatcher.matches())
			return result;

		Optional<String> outPath = SubstitutePath(inputMatcher, outputSubst);
		Optional<String> radPath = SubstitutePath(inputMatcher, radicalSubst.orElse(outputSubst));
		if (outPath.isPresent() && radPath.isPresent()){
			ResourceLocation outputId  = inputId.withPath(outPath.get());
			ResourceLocation radicalId = inputId.withPath(radPath.get());

			result.putIfAbsent(
				outputId,
				new ParentedResource(
					radicalId,
					template.Backfilled(Map.of("NAMESPACE", inputId.getNamespace()))
					        .Backfilled(FilledTemplate.IdVariables("INPUT",   pass.input .GetVanillaId(inputId )))
					        .Backfilled(FilledTemplate.IdVariables("OUTPUT",  pass.output.GetVanillaId(outputId)))
					        .Backfilled(FilledTemplate.IdVariables("RADICAL", pass.output.GetVanillaId(radicalId)))
				)
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
		if (!ResourceLocation.isValidPath(stringResult)){
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
