package fr.estecka.variantscit.assetgen;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import fr.estecka.variantscit.CodecUtil;
import fr.estecka.variantscit.VariantsCitMod;
import fr.estecka.variantscit.format.Substitution;

/**
 * TODO: Maybe reimplement using ReloadableRepository.
 * This will need to override the reload method with createTemplate, since this
 * is not codec based.
 */
public class TemplateRepository
{
	static private final String DIRECTORY = "variants-cit/templates";
	static private final String EXTENSION = ".json";
	static private final Map<ResourceLocation, Substitution> TEMPLATES = new HashMap<>();
	static public final Codec<Substitution> CODEC = CodecUtil.Enum(CodecUtil.VCIT_IDENTIFIER, TEMPLATES);

	static public DataResult<Substitution> Get(ResourceLocation id){
		Substitution result = TEMPLATES.get(id);
		return (result != null) ?
			DataResult.success(result) :
			DataResult.error(()->"No such template: "+id.toString());
	}

	static public void ReloadPatterns(ResourceManager manager)
	{
		TEMPLATES.clear();
		Map<ResourceLocation, Resource> resources = manager.listResources(DIRECTORY, id->id.getPath().endsWith(EXTENSION));

		for (var entry : resources.entrySet())
		{
			Resource resource = entry.getValue();
			ResourceLocation id = entry.getKey().withPath(path -> path.substring(DIRECTORY.length()+1, path.length()-EXTENSION.length()));

			var result = CreateTemplate(resource);
			if (result.isError())
				VariantsCitMod.LOGGER.error("Error loading template:{}\n{}", id, result.error().get().message());
			else
				TEMPLATES.put(id, result.getOrThrow());
		}
	}

	static private DataResult<Substitution> CreateTemplate(Resource resource){
		BufferedReader reader;
		try {
			reader = resource.openAsReader();
		}
		catch (IOException e){
			return DataResult.error(e::getMessage);
		}

		String raw = "";
		Iterator<String> lines = reader.lines().iterator();
		while (lines.hasNext()){
			raw += lines.next() + "\n";
		}

		return Substitution.Parse(raw);
	}
}
