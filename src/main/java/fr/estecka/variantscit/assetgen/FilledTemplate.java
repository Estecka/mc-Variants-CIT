package fr.estecka.variantscit.assetgen;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.IoSupplier;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.estecka.variantscit.CodecUtil;
import fr.estecka.variantscit.VariantsCitMod;
import fr.estecka.variantscit.format.Substitution;

/**
 * Binds some variables to a template. The template is never guaranteed to be
 * fully filled.
 */
public record FilledTemplate(
	Substitution rawTemplate,
	Map<String,String> variables
)
implements IoSupplier<InputStream>
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

	static public final Codec<FilledTemplate> CODEC = CodecUtil.WithAlternative(STRING_CODEC, MAPCODEC.codec());


/******************************************************************************/
/* # Supplier                                                                 */
/******************************************************************************/

	public String getString(){
		return this.rawTemplate.Substitute(this.variables);
	}

	@Override
	public InputStream get() throws IOException {
		String fullAsset = this.getString();
		return new ByteArrayInputStream(fullAsset.getBytes());
	}


/******************************************************************************/
/* # Builder Util                                                             */
/******************************************************************************/

	static public DataResult<FilledTemplate> ModelParent(String parent){
		return TemplateRepository.Get(VariantsCitMod.Identifier("models/parent"))
			.map(template -> new FilledTemplate(template, Map.of("modelParent", parent))
		);
	}

	static public DataResult<FilledTemplate> Stateless(){
		return TemplateRepository.Get(VariantsCitMod.Identifier("items/stateless"))
			.map(template -> new FilledTemplate(template, Map.of())
		);
	}


/******************************************************************************/
/* # Variables Util                                                           */
/******************************************************************************/

	static public HashMap<String,String> IdVariables(String varname, ResourceLocation identifier){
		HashMap<String,String> variables = new HashMap<>();

		variables.put(varname+"_ID", identifier.toString());
		variables.put(varname+"_PATH", identifier.getPath());
		variables.put(varname+"_NAMESPACE", identifier.getNamespace());

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
}
