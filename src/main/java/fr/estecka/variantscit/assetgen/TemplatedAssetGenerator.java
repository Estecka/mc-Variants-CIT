package fr.estecka.variantscit.assetgen;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import org.jetbrains.annotations.Nullable;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.estecka.variantscit.format.IStringTransform;
import fr.estecka.variantscit.format.Substitution;
import fr.estecka.variantscit.format.transforms.SuccessiveTransform;
import net.minecraft.resource.InputSupplier;
import net.minecraft.util.Identifier;

public record TemplatedAssetGenerator(
	EAssetGenPass pass,
	Substitution template,
	Predicate<Identifier> acceptedVariants,
	Map<String,String> variableOverrides
)
implements IAssetGenerator
{
	static public final MapCodec<TemplatedAssetGenerator> MAPCODEC = RecordCodecBuilder.mapCodec(builder->
		builder.group(
			EAssetGenPass.CODEC.fieldOf("pass").forGetter(TemplatedAssetGenerator::pass),
			TemplateRepository.CODEC.fieldOf("template").forGetter(TemplatedAssetGenerator::template),
			SuccessiveTransform.CODEC.optionalFieldOf("acceptedAssets", IStringTransform.NOOP).xmap(TemplatedAssetGenerator::TransformAsPredicate, _0->null).forGetter(TemplatedAssetGenerator::acceptedVariants),
			Codec.unboundedMap(Codec.STRING, Codec.STRING).optionalFieldOf("variables", Map.of()).forGetter(TemplatedAssetGenerator::variableOverrides)
		)
		.apply(builder, TemplatedAssetGenerator::new)
	);

	static public Predicate<Identifier> TransformAsPredicate(IStringTransform transform){
		return (id)->{ return transform.apply(id.toString()) != null; };
	}

	@Override
	public @Nullable InputSupplier<InputStream> AcceptAsset(EAssetGenPass pass, Identifier assetId) {
		if (pass != this.pass || !acceptedVariants.test(assetId))
			return null;

		HashMap<String,String> variables = TemplatedResource.DefaultVariables(assetId);
		variables.putAll(variableOverrides);
		return new TemplatedResource(this.template, variables);
	}

	public record Builder(
		EAssetGenPass pass,
		Identifier templateId,
		Predicate<Identifier> acceptedVariants,
		Map<String,String> variableOverrides
	)
	implements IAssetGenerator.Builder
	{
		@Override
		public DataResult<IAssetGenerator> get() {
			Substitution template = TemplateRepository.Get(templateId);
			if (template == null)
				return DataResult.error(()->"Missing template: " + templateId);
			else
				return DataResult.success(new TemplatedAssetGenerator(pass, template, acceptedVariants, variableOverrides));
		}
	}
}
