package fr.estecka.variantscit.assetgen;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import com.mojang.serialization.DataResult;
import fr.estecka.variantscit.VariantsCitMod;
import fr.estecka.variantscit.format.Substitution;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

public class TemplateRepository
{
	static private Map<Identifier, Substitution> TEMPLATES = Map.of();

	static public Substitution Get(Identifier id){
		return TEMPLATES.get(id);
	}

	static public void ReloadPatterns(ResourceManager manager)
	{
		TEMPLATES = new HashMap<>();
		Map<Identifier, Resource> resources = manager.findResources("variants-cit/templates", id->id.getPath().endsWith(".json"));

		for (var entry : resources.entrySet())
		{
			Resource resource = entry.getValue();
			Identifier id = entry.getKey().withPath(path -> path.substring("variants-cit/templates/".length(), path.length()-".json".length()));

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
