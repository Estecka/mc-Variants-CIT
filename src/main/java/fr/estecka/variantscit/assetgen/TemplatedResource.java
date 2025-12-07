package fr.estecka.variantscit.assetgen;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import fr.estecka.variantscit.format.Substitution;
import net.minecraft.resource.InputSupplier;
import net.minecraft.util.Identifier;

public record TemplatedResource(
	Substitution template,
	Map<String,String> variables
)
implements InputSupplier<InputStream>
{
	static public HashMap<String,String> DefaultVariables(Identifier assetId){
		HashMap<String,String> variables = new HashMap<>();
		variables.put("ASSET_ID", assetId.toString());
		variables.put("ASSET_PATH", assetId.getPath());
		variables.put("ASSET_NAMESPACE", assetId.getNamespace());
		variables.put("BAKED_MODEL_ID", assetId.getNamespace() + ":item/" + assetId.getPath());
		return variables;
	}

	@Override
	public InputStream get() throws IOException {
		String fullAsset = this.template.Substitute(variables);
		return new ByteArrayInputStream(fullAsset.getBytes());
	}
}
