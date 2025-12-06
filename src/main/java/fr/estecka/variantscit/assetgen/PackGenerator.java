package fr.estecka.variantscit.assetgen;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import net.minecraft.resource.InputSupplier;

public class PackGenerator
{

	static private final String MODEL = """
		{
			"parent": "item/handheld",
			"textures": {
				"layer0": "item/diamond"
			}
		}
	""";

	static public InputSupplier<InputStream> GetSupplier(){
		return () -> new ByteArrayInputStream(MODEL.getBytes());
	}

}
