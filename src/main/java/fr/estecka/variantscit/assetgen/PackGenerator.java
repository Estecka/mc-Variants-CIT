package fr.estecka.variantscit.assetgen;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashSet;

import net.minecraft.util.Identifier;

public class PackGenerator {

	static private final String MODEL = """
		{
			"parent": "item/handheld",
			"textures": {
				"layer0": "item/diamond"
			}
		}
	""";

	public HashSet<Identifier> resources = new HashSet<>();

	public void AddFile(Identifier resourceId){
		this.resources.add(resourceId);
	}

	public InputStream GetStream(Identifier resourceId){
		if (!this.resources.contains(resourceId))
			return null;

		return new ByteArrayInputStream(MODEL.getBytes());
	}

}
