package fr.estecka.variantscit.assetgen;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import com.mojang.serialization.Codec;
import fr.estecka.variantscit.CodecUtil;
import fr.estecka.variantscit.VariantsCitMod;
import fr.estecka.variantscit.reload.ModuleDefinition;

public final class GeneratorPresets
{
	static private final String DIRECTORY = "variants-cit/assetgen_presets";
	static private final String EXTENSION = ".json";

	static private final Map<Identifier, IAssetGenerator> BAKED_PRESETS = new HashMap<>();
	static public final Codec<IAssetGenerator> PRESET_CODEC = CodecUtil.Enum(CodecUtil.VCIT_IDENTIFIER, BAKED_PRESETS);

	static public void ReloadPresets(ResourceManager manager){
		var result = CodecUtil.ReloadResources(manager, TemplatedAssetGenerator.MAPCODEC.codec().listOf(), DIRECTORY, EXTENSION);
		BAKED_PRESETS.clear();
		for (var entry : result.entrySet())
			BAKED_PRESETS.put(entry.getKey(), IAssetGenerator.ListGenerator.Wrap(entry.getValue()));
	}

	static public IAssetGenerator LegacyGenerator(ModuleDefinition module){
		Pattern acceptAll = Pattern.compile(".*");
		IAssetGenerator items;
		IAssetGenerator models;

		if (!module.modelParent().isPresent())
			return IAssetGenerator.NOOP;

		var itemsTemplate = FilledTemplate.Stateless();
		if (itemsTemplate.isError()){
			VariantsCitMod.LOGGER.error("Missing built-in template for option itemsFromModel.\n{}", itemsTemplate.error().get().message());
			return IAssetGenerator.NOOP;
		}
		items  = new TemplatedAssetGenerator(EAssetGenPass.ITEM_STATES , acceptAll, itemsTemplate.getOrThrow());

		var modelTemplate = FilledTemplate.ModelParent(module.modelParent().get().toString());
		if (modelTemplate.isError()){
			VariantsCitMod.LOGGER.error("Missing built-in template for option modelParent.\n{}", modelTemplate.error().get().message());
			return items;
		}
		models = new TemplatedAssetGenerator(EAssetGenPass.BAKED_MODELS, acceptAll, modelTemplate.getOrThrow());

		return IAssetGenerator.ListGenerator.Of(items, models);
	}
}
