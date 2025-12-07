package fr.estecka.variantscit.assetgen;

import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import com.mojang.serialization.Codec;
import fr.estecka.variantscit.CodecUtil;
import fr.estecka.variantscit.VariantsCitMod;
import fr.estecka.variantscit.assetgen.IAssetGenerator.ListBuilder;
import fr.estecka.variantscit.assetgen.TemplatedAssetGenerator.Builder;
import fr.estecka.variantscit.reload.ModuleDefinition;
import net.minecraft.util.Identifier;

public class GeneratorsRegistry
{
	static private final Pattern BOW_SUBVARIANTS = Pattern.compile(".+_pulling_[0-9]+");
	static private final Pattern TRIDENT_SUBVARIANTS = Pattern.compile(".+(_in_hand|_throwing)");

	static private final Builder MODELS_GENERATED = new Builder(EAssetGenPass.BAKED_MODELS, Identifier.ofVanilla("models/item/generated"), id->true, Map.of());
	static private final Builder MODELS_HANDHELD  = new Builder(EAssetGenPass.BAKED_MODELS, Identifier.ofVanilla("models/item/handheld"),  id->true, Map.of());

	static private final Builder ITEMS_STATELESS        = new Builder(EAssetGenPass.ITEM_STATES, Identifier.ofVanilla("items/stateless"), id->true, Map.of());
	static private final Builder ITEMS_TRIDENT_GUI_ONLY = new Builder(EAssetGenPass.ITEM_STATES, Identifier.ofVanilla("items/trident_gui_only"), id->true, Map.of());
	static private final Builder ITEMS_TRIDENT          = new Builder(EAssetGenPass.ITEM_STATES, Identifier.ofVanilla("items/trident"), ExcludeRegex(TRIDENT_SUBVARIANTS), Map.of());
	static private final Builder ITEMS_BOW              = new Builder(EAssetGenPass.ITEM_STATES, Identifier.ofVanilla("items/bow"), ExcludeRegex(BOW_SUBVARIANTS), Map.of());

	static public final Codec<IAssetGenerator> PRESET_CODEC = CodecUtil.Enum(Codec.STRING, Map.of(
		"item/generated",        ListBuilder.Of(ITEMS_STATELESS, MODELS_GENERATED),
		"item/handheld",         ListBuilder.Of(ITEMS_STATELESS, MODELS_HANDHELD),
		"item/bow",              ListBuilder.Of(ITEMS_BOW, ModelParent("item/bow")),
		"item/trident",          ListBuilder.Of(ITEMS_TRIDENT, MODELS_HANDHELD),
		"item/trident_gui_only", ListBuilder.Of(ITEMS_TRIDENT_GUI_ONLY, MODELS_GENERATED)
	)).flatXmap(IAssetGenerator.Builder::get, _0->null);

	static private Builder ModelParent(String parent){
		return new Builder(EAssetGenPass.BAKED_MODELS, Identifier.ofVanilla("models/model_parent"), id->true, Map.of("MODEL_PARENT", parent));
	}

	static private Builder ModelParent(Identifier parent){
		return ModelParent(parent.toString());
	}

	static public Predicate<Identifier> ExcludeRegex(Pattern regex){
		return (Identifier id) -> !regex.matcher(id.getPath()).matches();
	}

	static public IAssetGenerator LegacyGenerator(ModuleDefinition module){
		IAssetGenerator items;
		IAssetGenerator models;
		if (!module.itemGen())
			return IAssetGenerator.NOOP;

		var optItems = ITEMS_STATELESS.get();
		if (optItems.isError()){
			VariantsCitMod.LOGGER.error("Bad item state generator: {}", optItems.error().get().message());
			return IAssetGenerator.NOOP;
		}

		items = optItems.getOrThrow();

		if (!module.modelParent().isPresent())
			return items;

		var optModels = ModelParent(module.modelParent().get()).get();
		if (optModels.isError()){
			VariantsCitMod.LOGGER.error("Bad baked model generator: {}", optModels.error().get().message());
			return items;
		}

		models = optModels.getOrThrow();

		return IAssetGenerator.OfList(items, models);
	}
}
