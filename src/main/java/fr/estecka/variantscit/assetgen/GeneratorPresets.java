package fr.estecka.variantscit.assetgen;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import fr.estecka.variantscit.util.CodecUtil;
import fr.estecka.variantscit.assetgen.generators.IAssetGenerator;
import fr.estecka.variantscit.assetgen.generators.GeneratorList;
import fr.estecka.variantscit.assetgen.generators.TemplatedAssetGenerator;

public final class GeneratorPresets
{
	static private final String DIRECTORY = "variants-cit/assetgen_presets";
	static private final String EXTENSION = ".json";

	// TODO: Use ReloadableRepository
	static private final Map<Identifier, IAssetGenerator> BAKED_PRESETS = new HashMap<>();
	static public final Codec<IAssetGenerator> PRESET_CODEC = CodecUtil.Enum(CodecUtil.VCIT_IDENTIFIER, BAKED_PRESETS);
	static public final Codec<IAssetGenerator> MODELPARENT_CODEC = Identifier.CODEC.comapFlatMap(GeneratorPresets::ModelParent, CodecUtil.NoGetter("ModelPrent generator"));
	static public final MapCodec<IAssetGenerator> PRESET_MAPCODEC = PRESET_CODEC.fieldOf("preset");
	static public final MapCodec<IAssetGenerator> MODELPARENT_MAPCODEC = MODELPARENT_CODEC.fieldOf("modelParent");

	static public void ReloadPresets(ResourceManager manager){
		var result = CodecUtil.ReloadResources(manager, TemplatedAssetGenerator.MAPCODEC.codec().listOf(), DIRECTORY, EXTENSION);
		BAKED_PRESETS.clear();
		for (var entry : result.entrySet())
			BAKED_PRESETS.put(entry.getKey(), GeneratorList.Wrap(entry.getValue()));
	}

	static public DataResult<IAssetGenerator> ModelParent(Identifier modelParent){
		Pattern acceptAll = Pattern.compile(".*");
		IAssetGenerator items;
		IAssetGenerator models;

		var itemsTemplate = FilledTemplate.Stateless();
		if (itemsTemplate.isError())
			return DataResult.error(() -> "Missing built-in template: \n"+itemsTemplate.error().get().message());

		items  = new TemplatedAssetGenerator(EAssetGenPass.ITEM_STATES , acceptAll, itemsTemplate.getOrThrow());

		var modelTemplate = FilledTemplate.ModelParent(modelParent.toString());
		if (modelTemplate.isError()){
			return DataResult.error(()->"Missing built-in template: \n"+modelTemplate.error().get().message());
		}
		models = new TemplatedAssetGenerator(EAssetGenPass.BAKED_MODELS, acceptAll, modelTemplate.getOrThrow());

		return DataResult.success(GeneratorList.Of(items, models));
	}
}
