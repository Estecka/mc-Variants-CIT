package fr.estecka.variantscit.assetgen;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.estecka.variantscit.format.Substitution;
import net.minecraft.resource.InputSupplier;
import net.minecraft.util.Identifier;

public record FilledTemplate(
	Substitution rawTemplate,
	Map<String,String> variables
)
implements InputSupplier<InputStream>
{
	static public final Codec<FilledTemplate> STRING_CODEC = TemplateRepository.CODEC.xmap(
		subst -> new FilledTemplate(subst, Map.of()),
		FilledTemplate::rawTemplate
	);

	static public final MapCodec<FilledTemplate> MAPCODEC = RecordCodecBuilder.mapCodec(builder->
		builder.group(
			TemplateRepository.CODEC.fieldOf("template").forGetter(FilledTemplate::rawTemplate),
			Codec.unboundedMap(Codec.STRING, Codec.STRING).optionalFieldOf("variables", Map.of()).forGetter(FilledTemplate::variables)
		)
		.apply(builder, FilledTemplate::new)
	);

	static public final Codec<FilledTemplate> CODEC = Codec.withAlternative(STRING_CODEC, MAPCODEC.codec());

	static public HashMap<String,String> DefaultVariables(Identifier assetId){
		HashMap<String,String> variables = new HashMap<>();

		variables.put("ASSET_ID", assetId.toString());
		variables.put("ASSET_PATH", assetId.getPath());
		variables.put("ASSET_NAMESPACE", assetId.getNamespace());
		variables.put("BAKED_MODEL_ID", assetId.getNamespace() + ":item/" + assetId.getPath());

		// variables.put("MASTER_ID", masterId.toString());
		// variables.put("MASTER_PATH", masterId.getPath());
		// variables.put("MASTER_NAMESPACE", masterId.getNamespace());
		// variables.put("MASTER_BAKED_MODEL_ID", masterId.getNamespace() + ":item/" + masterId.getPath());

		return variables;
	}

	public FilledTemplate Backfilled(Map<String,String> backVariables){
		Map<String,String> allVariables = new HashMap<>();
		allVariables.putAll(backVariables);
		allVariables.putAll(this.variables);
		return new FilledTemplate(this.rawTemplate, allVariables);
	}

	public FilledTemplate Frontfilled(Map<String,String> frontVariables){
		Map<String,String> allVariables = new HashMap<>();
		allVariables.putAll(this.variables);
		allVariables.putAll(frontVariables);
		return new FilledTemplate(this.rawTemplate, allVariables);
	}

	public String getString(){
		return this.rawTemplate.Substitute(this.variables);
	}

	@Override
	public InputStream get() throws IOException {
		String fullAsset = this.getString();
		return new ByteArrayInputStream(fullAsset.getBytes());
	}
}
