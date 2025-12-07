package fr.estecka.variantscit.assetgen;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import org.jetbrains.annotations.Nullable;
import com.mojang.serialization.DataResult;
import fr.estecka.variantscit.format.Substitution;
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
