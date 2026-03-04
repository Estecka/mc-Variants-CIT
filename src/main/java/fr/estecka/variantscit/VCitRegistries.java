package fr.estecka.variantscit;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import java.util.Map;
import java.util.Optional;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapDecoder;
import fr.estecka.variantscit.modules.libraries.IVariantCitModule;
import fr.estecka.variantscit.itemdata.extractors.IDataExtractor;
import fr.estecka.variantscit.itemdata.extractors.TransformableExtractor;
import fr.estecka.variantscit.itemdata.extractors.impl.*;
import fr.estecka.variantscit.itemdata.transforms.IDataConversions;
import fr.estecka.variantscit.itemdata.transforms.IDataTransform;
import fr.estecka.variantscit.itemdata.transforms.IStringTransform;
import fr.estecka.variantscit.itemdata.transforms.OptionalTransform;
import fr.estecka.variantscit.itemdata.transforms.SuccessiveTransform;
import fr.estecka.variantscit.itemdata.transforms.impl.*;
import fr.estecka.variantscit.itemdata.preconditions.*;
import fr.estecka.variantscit.modules.*;
import fr.estecka.variantscit.modules.impl.*;
import fr.estecka.variantscit.modules.libraries.*;
import fr.estecka.variantscit.reload.UnbakedModule;

public final class VCitRegistries
{
	static public final DecodableRegistry<UnbakedModule<?>> MODULES = new DecodableRegistry<>("type", VCitRegistries::OptionalParameters);
	static public final DecodableRegistry<IDataExtractor> ITEM_PROPERTIES = new DecodableRegistry<>("property", ResourceLocation.withDefaultNamespace("item_component"), TransformableExtractor::CodecOf);
	static public final DecodableRegistry<IDataTransform> TRANSFORMS = new DecodableRegistry<>("function", ResourceLocation.withDefaultNamespace("auto"), OptionalTransform::CodecOf);
	static public final DecodableRegistry<IDataConversions<?>> NBT_INPUTS = new DecodableRegistry<>("type");

	static public final DecodableRegistry<IItemPrecondition> PRECONDITIONS = new DecodableRegistry<>("condition", ResourceLocation.withDefaultNamespace("transform"));

	static {
		RegisterBakedModule (ResourceLocation.withDefaultNamespace("axolotl_variant"), AxolotlBucketModule.CODEC, AxolotlBucketModule.BAKER);
		RegisterSimpleModule(ResourceLocation.withDefaultNamespace("component_data"), ComponentDataModule.CODEC);
		RegisterSimpleModule(ResourceLocation.withDefaultNamespace("component_format"), MultiComponentFormatModule.CODEC);
		RegisterBakedModule (ResourceLocation.withDefaultNamespace("component_threshold"), ComponentThresholdModule.MAPCODEC, LinearLibrary.GetBaker());
		RegisterSimpleModule(ResourceLocation.withDefaultNamespace("custom_name"), CustomNameModule.CODEC);
		RegisterBakedModule (ResourceLocation.withDefaultNamespace("durability"), DurabilityModule.CODEC, LinearLibrary.GetBaker());
		RegisterSimpleModule(ResourceLocation.withDefaultNamespace("enchantment"), EnchantmentModule.CreateCodec(DataComponents.ENCHANTMENTS));
		RegisterBakedModule (ResourceLocation.withDefaultNamespace("enchantment_vector"), EnchantmentVectorModule.PARAM_MAPCODEC, EnchantmentVectorModule.GetBaker(DataComponents.ENCHANTMENTS));
		RegisterSimpleModule(ResourceLocation.withDefaultNamespace("instrument"), GoatHornModule.UNIT);
		RegisterBakedModule (ResourceLocation.withDefaultNamespace("item_count"), ItemCountModule.CODEC, LinearLibrary.GetBaker());
		RegisterSimpleModule(ResourceLocation.withDefaultNamespace("jukebox_playable"), MusicDiscModule.UNIT);
		RegisterSimpleModule(ResourceLocation.withDefaultNamespace("painting_variant"), PaintingVariantModule.UNIT);
		RegisterSimpleModule(ResourceLocation.withDefaultNamespace("potion_effect"), PotionEffectModule.UNIT);
		RegisterSimpleModule(ResourceLocation.withDefaultNamespace("potion_type"), PotionTypeModule.UNIT);
		RegisterSimpleModule(ResourceLocation.withDefaultNamespace("stored_enchantment"), EnchantmentModule.CreateCodec(DataComponents.STORED_ENCHANTMENTS));
		RegisterSimpleModule(ResourceLocation.withDefaultNamespace("stored_enchantments"), CodecUtil.WithWarning(
			MapCodec.unit(new EnchantmentModule(DataComponents.STORED_ENCHANTMENTS, Map.of(), Optional.empty())),
			"Module name `stored_enchantments` (plural) is being deprecated. use `stored_enchantment` (singular) instead."
		));
		RegisterBakedModule (ResourceLocation.withDefaultNamespace("stored_enchantment_vector"), EnchantmentVectorModule.PARAM_MAPCODEC, EnchantmentVectorModule.GetBaker(DataComponents.STORED_ENCHANTMENTS));
		RegisterSimpleModule(ResourceLocation.withDefaultNamespace("trim"), TrimModule.UNIT);
		RegisterSimpleModule(ResourceLocation.withDefaultNamespace("trim_pattern"), TrimPatternModule.UNIT);
		RegisterSimpleModule(ResourceLocation.withDefaultNamespace("trim_material"), TrimPatternModule.UNIT);

		RegisterRemoved("block_entity_data",  "component_data");
		RegisterRemoved("bucket_entity_data", "component_data");
		RegisterRemoved("custom_data",        "component_data");
		RegisterRemoved("entity_data",        "component_data");

		ITEM_PROPERTIES.RegisterUnit(ResourceLocation.withDefaultNamespace("axolotl_variant"), AxolotlVariantProperty.UNIT);
		ITEM_PROPERTIES.Register(ResourceLocation.withDefaultNamespace("bucket_entity_age"), EntityAgeMapProperty.MAP_CODEC, EntityAgeMapProperty.UNIT);
		ITEM_PROPERTIES.RegisterMap(ResourceLocation.withDefaultNamespace("item_component"), ItemComponentProperty.MAP_CODEC);
		ITEM_PROPERTIES.RegisterUnit(ResourceLocation.withDefaultNamespace("item_count"), ItemCountProperty.UNIT);
		ITEM_PROPERTIES.RegisterUnit(ResourceLocation.withDefaultNamespace("item_type"), ItemTypeProperty.UNIT);
		ITEM_PROPERTIES.RegisterUnit(ResourceLocation.withDefaultNamespace("painting_variant"), PaintingVariantProperty.UNIT);

		TRANSFORMS.RegisterUnit(ResourceLocation.withDefaultNamespace("noop"),               IStringTransform.NOOP);
		TRANSFORMS.RegisterUnit(ResourceLocation.withDefaultNamespace("null"),               IStringTransform.NULL);
		TRANSFORMS.RegisterUnit(ResourceLocation.withDefaultNamespace("lowercase"),          (IStringTransform)String::toLowerCase);
		TRANSFORMS.RegisterUnit(ResourceLocation.withDefaultNamespace("discard_path"),       (IStringTransform)IStringTransform::DiscardPath);
		TRANSFORMS.RegisterUnit(ResourceLocation.withDefaultNamespace("discard_namespace"),  (IStringTransform)IStringTransform::DiscardNamespace);
		TRANSFORMS.RegisterUnit(ResourceLocation.withDefaultNamespace("sanitize"),           IStringTransform.SANITIZE);
		TRANSFORMS.RegisterUnit(ResourceLocation.withDefaultNamespace("sanitize_path"),      IStringTransform.SANITIZE_PATH);
		TRANSFORMS.RegisterUnit(ResourceLocation.withDefaultNamespace("sanitize_namespace"), IStringTransform.SANITIZE_NAMESPACE);
		TRANSFORMS.RegisterUnit(ResourceLocation.withDefaultNamespace("sanitize_auto"),      (IStringTransform)IStringTransform::AutoSanitize);
		TRANSFORMS.RegisterMap(ResourceLocation.withDefaultNamespace("test"),                TestTransform.MAPCODEC);
		TRANSFORMS.RegisterMap(ResourceLocation.withDefaultNamespace("successive"),          SuccessiveTransform.MAPCODEC);
		TRANSFORMS.RegisterMap(ResourceLocation.withDefaultNamespace("alternative"),         AlternativeTransform.MAPCODEC);
		TRANSFORMS.RegisterMap(ResourceLocation.withDefaultNamespace("whitelist"),           FilterlistTransform.MAPCODEC_WHITELIST);
		TRANSFORMS.RegisterMap(ResourceLocation.withDefaultNamespace("blacklist"),           FilterlistTransform.MAPCODEC_BLACKLIST);
		TRANSFORMS.RegisterMap(ResourceLocation.withDefaultNamespace("charset_remap"),       CharRemapTransform.MAPCODEC);
		TRANSFORMS.RegisterMap(ResourceLocation.withDefaultNamespace("remap"),               RemapTransform.MAPCODEC);
		TRANSFORMS.RegisterMap(ResourceLocation.withDefaultNamespace("regex"),               RegexTransform.MAPCODEC);
		TRANSFORMS.RegisterMap(ResourceLocation.withDefaultNamespace("nbt_path"),            NbtPath.MAPCODEC);

		TRANSFORMS.RegisterMap(ResourceLocation.withDefaultNamespace("equals"),              CodecUtil.MapWithAlternative(StringCompareTransform.MAPCODEC,NumberCompareTransform.MAPCODEC_EQUAL));
		TRANSFORMS.RegisterMap(ResourceLocation.withDefaultNamespace("smaller_than"),        NumberCompareTransform.MAPCODEC_SMALLER);
		TRANSFORMS.RegisterMap(ResourceLocation.withDefaultNamespace("greater_than"),        NumberCompareTransform.MAPCODEC_GREATER);
		TRANSFORMS.RegisterMap(ResourceLocation.withDefaultNamespace("smaller_or_equals"),   NumberCompareTransform.MAPCODEC_GREAT_OR_EQ);
		TRANSFORMS.RegisterMap(ResourceLocation.withDefaultNamespace("greater_or_equals"),   NumberCompareTransform.MAPCODEC_SMALL_OR_EQ);

		TRANSFORMS.RegisterMap(ResourceLocation.withDefaultNamespace("auto"), CodecUtil.MapWithAlternatives(
			RegexTransform.MAPCODEC,
			FilterlistTransform.MAPCODEC_BLACKLIST,
			FilterlistTransform.MAPCODEC_WHITELIST,
			StringCompareTransform.MAPCODEC,
			NumberCompareTransform.MAPCODEC_EQUAL,
			NumberCompareTransform.MAPCODEC_GREATER,
			NumberCompareTransform.MAPCODEC_SMALLER,
			NumberCompareTransform.MAPCODEC_GREAT_OR_EQ,
			NumberCompareTransform.MAPCODEC_SMALL_OR_EQ
		));

		NBT_INPUTS.RegisterUnit(ResourceLocation.withDefaultNamespace("auto"),            IDataConversions.TO_STRING);
		NBT_INPUTS.RegisterUnit(ResourceLocation.withDefaultNamespace("primitive"),       IDataConversions::SoftCastToString);
		NBT_INPUTS.RegisterUnit(ResourceLocation.withDefaultNamespace("string"),          IDataConversions::SoftCastToString);
		NBT_INPUTS.RegisterUnit(ResourceLocation.withDefaultNamespace("number"),          IDataConversions::SoftCastToNumber);
		NBT_INPUTS.RegisterUnit(ResourceLocation.withDefaultNamespace("identifier"),      IDataConversions::SoftCastToId);
		NBT_INPUTS.RegisterUnit(ResourceLocation.withDefaultNamespace("rich_text"),       IDataConversions::SoftCastToText);
		NBT_INPUTS.RegisterUnit(ResourceLocation.withDefaultNamespace("rich_text_array"), IDataConversions::LooseCastTextArray);

		PRECONDITIONS.RegisterMap(ResourceLocation.withDefaultNamespace("matches_all"), MatchesAllCondition.MAPCODEC);
		PRECONDITIONS.RegisterMap(ResourceLocation.withDefaultNamespace("matches_any"), MatchesAnyCondition.MAPCODEC);
		PRECONDITIONS.RegisterMap(ResourceLocation.withDefaultNamespace("transform"),   ITEM_PROPERTIES.mapCodec);
	}

	static public <T> void RegisterBakedModule(ResourceLocation id, MapCodec<T> mapcodec, IModuleBaker<T> baker){
		MODULES.RegisterMap(id, mapcodec.xmap(
			parameters -> new UnbakedModule<>(baker, parameters),
			UnbakedModule::parameters
		));
	}

	static public void RegisterSimpleModule(ResourceLocation id, MapCodec<? extends IVariantCitModule> mapcodec){
		RegisterBakedModule(id, mapcodec, VariantLibrary::Bake);
	}

	static public void RegisterSimpleModule(ResourceLocation id, IVariantCitModule unit){
		RegisterSimpleModule(id, MapCodec.unit(unit));
	}

	static private <T> MapDecoder<T> OptionalParameters(MapDecoder<T> mapcodec){
		return CompoundTag.CODEC
			.optionalFieldOf("parameters", new CompoundTag())
			.flatMap(nbt -> NbtOps.INSTANCE.withParser(mapcodec.decoder()).apply(nbt))
			;
	}

	static private void RegisterRemoved(String oldName, String newName){
		RegisterSimpleModule(
			ResourceLocation.withDefaultNamespace(oldName),
			MapCodec.unit(PaintingVariantModule.UNIT).flatXmap(
				_0->DataResult.error(()->"Module type `"+oldName+"` was removed, use `"+newName+"` isntead"),
				_0->null
			)
		);
	}
}
