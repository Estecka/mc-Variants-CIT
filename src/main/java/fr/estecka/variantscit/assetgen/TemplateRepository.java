package fr.estecka.variantscit.assetgen;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import fr.estecka.variantscit.CodecUtil;
import fr.estecka.variantscit.VariantsCitMod;
import fr.estecka.variantscit.format.Substitution;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

public class TemplateRepository
{
	static private final String DIRECTORY = "variants-cit/templates";
	static private final String EXTENSION = ".json";
	static private final Map<Identifier, Substitution> TEMPLATES = new HashMap<>();
	static public final Codec<Substitution> CODEC = CodecUtil.Enum(Identifier.CODEC, TEMPLATES);

	static public Substitution Get(Identifier id){
		return TEMPLATES.get(id);
	}

	static public void ReloadPatterns(ResourceManager manager)
	{
		TEMPLATES.clear();
		Map<Identifier, Resource> resources = manager.findResources(DIRECTORY, id->id.getPath().endsWith(EXTENSION));

		for (var entry : resources.entrySet())
		{
			Resource resource = entry.getValue();
			Identifier id = entry.getKey().withPath(path -> path.substring(DIRECTORY.length()+1, path.length()-EXTENSION.length()));

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
			reader = resource.getReader();
		}
		catch (IOException e){
			return DataResult.error(e::getMessage);
		}

		String raw = "";
		Iterator<String> lines = reader.lines().iterator();
		while (lines.hasNext()){
			raw += lines.next();
		}

		return Substitution.Parse(raw);
	}
}
