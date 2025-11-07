package fr.estecka.variantscit.reload;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

/**
 * https://github.com/Estecka/mc-Variants-CIT/issues/87
 * 
 * Exploring  zip files multiple times  is pretty slow, so dumping everything in
 * memory first  is faster. This is just  a cheap  workaround; in the  future it
 * will be  preferable  to have  the VariantAggregators  run in parallel  over a
 * single iteration, instead of one after another.
 */
public class VCitResourceManager
{
	public final List<Identifier> textures = new ArrayList<>();
	public final List<Identifier> models = new ArrayList<>();
	public final List<Identifier> items = new ArrayList<>();
	public final List<Identifier> equipments = new ArrayList<>();

	static public VCitResourceManager GatherAll(ResourceManager manager){
		VCitResourceManager vcitManager = new VCitResourceManager();

		Gather(manager, "textures",  "item/",   ".png",  vcitManager.textures  );
		Gather(manager, "models",    "item/",   ".json", vcitManager.models    );
		Gather(manager, "items",     "",        ".json", vcitManager.items     );
		Gather(manager, "equipment", "",        ".json", vcitManager.equipments);

		return vcitManager;
	}

	static private void Gather(ResourceManager manager, String type, String prefix, String format, List<Identifier> list){
		String fullPrefix = type+'/'+prefix;

		Set<Identifier> resources = manager.findResources(
			type,
			id -> id.getPath().startsWith(fullPrefix) && id.getPath().endsWith(format)
		).keySet();

		Stream<Identifier> ids = resources.stream().map(
			id->id.withPath(path->path.substring(
				type.length() + 1,
				path.length() - format.length()
			))
		);

		ids.forEach(list::add);
	}

}
