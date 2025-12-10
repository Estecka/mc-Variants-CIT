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
	Map<String,FilledTemplate> outputs
)
implements IAssetGenerator
{
	static public final MapCodec<Map<String,FilledTemplate>> OUTPUT_MAPCODEC = CodecUtil.MapWithAlternative(
		Codec.unboundedMap(Codec.STRING, FilledTemplate.MAPCODEC.codec()).fieldOf("output"),
		FilledTemplate.MAPCODEC.xmap(template -> Map.of("$0", template), _0->null)
	);

	static public final MapCodec<TemplatedAssetGenerator> MAPCODEC = RecordCodecBuilder.mapCodec(builder->
		builder.group(
			EAssetGenPass.CODEC.fieldOf("pass").forGetter(TemplatedAssetGenerator::pass),
			CodecUtil.REGEX.optionalFieldOf("input", Pattern.compile(".*")).forGetter(TemplatedAssetGenerator::inputRegex),
			OUTPUT_MAPCODEC.forGetter(TemplatedAssetGenerator::outputs)
		)
		.apply(builder, TemplatedAssetGenerator::new)
	);

	public TemplatedAssetGenerator(
		EAssetGenPass pass,
		Pattern inputRegex,
		FilledTemplate template
	){
		this(pass, inputRegex, Map.of("$0", template));
	}

	@Override
	public IAssetGenerator.Result AcceptAsset(EAssetGenPass pass, Identifier assetId) {
		IAssetGenerator.Result result = new Result();
		Matcher inputMatcher = inputRegex.matcher(assetId.toString());
		if (pass != this.pass || !inputMatcher.matches())
			return result;

		// inputMatcher.replaceAll(radicalSubst);

		// Identifier radicalId = Substitute(inputMatcher, radicalSubst).orElse(assetId);

		Map<String,String> commonVariables = FilledTemplate.DefaultVariables(assetId);
		for (var entry : this.outputs.entrySet()){
			var optId = Substitute(inputMatcher, entry.getKey());
			if (optId.isPresent())
				result.putIfAbsent(optId.get(), entry.getValue().Backfilled(commonVariables));
		}

		return result;
	}

	static private Optional<Identifier> Substitute(Matcher matcher, String substitution){
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

		Identifier result = Identifier.of(stringResult);
		if (result == null){
			VariantsCitMod.LOGGER.error(
				"Asset Generator resulted in invalid identifier: {}\n- Regex: {}\n- Substitution: {}",
				stringResult,
				matcher.pattern().pattern(),
				substitution
			);
			return Optional.empty();
		}

		return Optional.of(result);
	}
}
