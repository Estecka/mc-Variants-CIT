package fr.estecka.variantscit;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapDecoder;
import fr.estecka.variantscit.modules.libraries.IVariantCitModule;
import fr.estecka.variantscit.itemdata.extractors.IDataExtractor;
import fr.estecka.variantscit.itemdata.extractors.TransformableExtractor;
import fr.estecka.variantscit.itemdata.extractors.impl.*;
import fr.estecka.variantscit.itemdata.transforms.DataConversions;
import fr.estecka.variantscit.itemdata.transforms.IDataTransform;
import fr.estecka.variantscit.itemdata.transforms.IStringTransform;
import fr.estecka.variantscit.itemdata.transforms.OptionalTransform;
import fr.estecka.variantscit.itemdata.transforms.SuccessiveTransform;
import fr.estecka.variantscit.itemdata.transforms.impl.*;
import fr.estecka.variantscit.itemdata.preconditions.*;
import fr.estecka.variantscit.modules.impl.*;
import fr.estecka.variantscit.modules.libraries.*;
import fr.estecka.variantscit.reload.IUnbakedModule;


public final class VCitRegistries
{
	static public final DecodableRegistry<IUnbakedModule> MODULES =
		new DecodableRegistry
			.Builder<IUnbakedModule>("type")
			.WithWrapper(VCitRegistries::OptionalParameters)
			.Build()
			;

	static public final DecodableRegistry<IDataExtractor> ITEM_PROPERTIES = 
		new DecodableRegistry
			.Builder<IDataExtractor>("property")
			.WithDefault(VariantsCitMod.Identifier("item_component"))
			.WithWrapper(TransformableExtractor::CodecOf)
			.Build()
			;

	static public final DecodableRegistry<IDataTransform> TRANSFORMS = 
		new DecodableRegistry
			.Builder<IDataTransform>("function")
			.WithDefault(VariantsCitMod.Identifier("auto_detect_function"))
			.WithWrapper(OptionalTransform::CodecOf)
			.Build()
			;

	static public final DecodableRegistry<IItemPrecondition> PRECONDITIONS = 
		new DecodableRegistry
			.Builder<IItemPrecondition>("condition")
			.WithDefault(VariantsCitMod.Identifier("transform"))
			.WithWrapper(NegativeCondition::CodecOf)
			.Build()
			;

	static
	{
		// ## Modules

		RegisterBakedModule (VariantsCitMod.Identifier("axolotl_variant"), AxolotlBucketModule.UNBAKED_MAPCODEC);
		RegisterSimpleModule(VariantsCitMod.Identifier("component_data"), ComponentDataModule.MAPCODEC);
		RegisterSimpleModule(VariantsCitMod.Identifier("component_format"), MultiComponentFormatModule.MAPCODEC);
		RegisterLinearModule(VariantsCitMod.Identifier("component_threshold"), ComponentThresholdModule.MAPCODEC);
		RegisterBakedModule (VariantsCitMod.Identifier("constant"), ConstantModule.MAPCODEC);
		RegisterSimpleModule(VariantsCitMod.Identifier("custom_name"), CustomNameModule.MAPCODEC);
		RegisterLinearModule(VariantsCitMod.Identifier("durability"), DurabilityModule.MAPCODEC);
		RegisterSimpleModule(VariantsCitMod.Identifier("enchantment"), EnchantmentModule.CreateCodec(DataComponents.ENCHANTMENTS));
		RegisterBakedModule (VariantsCitMod.Identifier("enchantment_vector"), EnchantmentVectorModule.GetBaker(DataComponents.ENCHANTMENTS));
		RegisterBakedModule (VariantsCitMod.Identifier("group"), GroupModule.Unbaked.MAPCODEC);
		RegisterSimpleModule(VariantsCitMod.Identifier("instrument"), GoatHornModule.UNIT);
		RegisterLinearModule(VariantsCitMod.Identifier("item_count"), ItemCountModule.UNIT);
		RegisterSimpleModule(VariantsCitMod.Identifier("jukebox_playable"), MusicDiscModule.UNIT);
		RegisterSimpleModule(VariantsCitMod.Identifier("painting_variant"), PaintingVariantModule.UNIT);
		RegisterSimpleModule(VariantsCitMod.Identifier("potion_effect"), PotionEffectModule.UNIT);
		RegisterSimpleModule(VariantsCitMod.Identifier("potion_type"), PotionTypeModule.UNIT);
		RegisterBakedModule (VariantsCitMod.Identifier("predicates"), PredicatesModule.Unbaked.MAPCODEC);
		RegisterSimpleModule(VariantsCitMod.Identifier("stored_enchantment"),  EnchantmentModule.CreateCodec(DataComponents.STORED_ENCHANTMENTS));
		RegisterSimpleModule(VariantsCitMod.Identifier("stored_enchantments"), EnchantmentModule.CreateCodec(DataComponents.STORED_ENCHANTMENTS));
		MODULES.Deprecate   (VariantsCitMod.Identifier("stored_enchantments"), "Module name `stored_enchantments` (plural) is deprecated. use `stored_enchantment` (singular) instead.");
		RegisterBakedModule (VariantsCitMod.Identifier("stored_enchantment_vector"), EnchantmentVectorModule.GetBaker(DataComponents.STORED_ENCHANTMENTS));
		RegisterSimpleModule(VariantsCitMod.Identifier("trim"), TrimModule.UNIT);
		RegisterSimpleModule(VariantsCitMod.Identifier("trim_pattern"), TrimPatternModule.UNIT);
		RegisterSimpleModule(VariantsCitMod.Identifier("trim_material"), TrimPatternModule.UNIT);

		RegisterRemoved("block_entity_data",  "component_data");
		RegisterRemoved("bucket_entity_data", "component_data");
		RegisterRemoved("custom_data",        "component_data");
		RegisterRemoved("entity_data",        "component_data");


		// ## Properties

		ITEM_PROPERTIES.RegisterUnit(VariantsCitMod.Identifier("axolotl_variant"), AxolotlVariantProperty.UNIT);
		ITEM_PROPERTIES.Register(VariantsCitMod.Identifier("bucket_entity_age"), EntityAgeMapProperty.MAP_CODEC, EntityAgeMapProperty.UNIT);
		ITEM_PROPERTIES.RegisterUnit(VariantsCitMod.Identifier("display_name"), DisplayNameProperty.UNIT);
		ITEM_PROPERTIES.RegisterMap(VariantsCitMod.Identifier("item_component"), ItemComponentProperty.MAP_CODEC);
		ITEM_PROPERTIES.RegisterUnit(VariantsCitMod.Identifier("item_count"), ItemCountProperty.UNIT);
		ITEM_PROPERTIES.RegisterUnit(VariantsCitMod.Identifier("item_type"), ItemTypeProperty.UNIT);
		ITEM_PROPERTIES.RegisterUnit(VariantsCitMod.Identifier("painting_variant"), PaintingVariantProperty.UNIT);


		// ## Transforms

		TRANSFORMS.RegisterUnit(VariantsCitMod.Identifier("noop"), IDataTransform.NOOP);
		TRANSFORMS.RegisterUnit(VariantsCitMod.Identifier("null"), IDataTransform.NULL);
		TRANSFORMS.Register    (VariantsCitMod.Identifier("log"),  LogTransform.MAPCODEC, new LogTransform());

		TRANSFORMS.RegisterUnit(VariantsCitMod.Identifier("lowercase"),          (IStringTransform)String::toLowerCase);
		TRANSFORMS.RegisterUnit(VariantsCitMod.Identifier("discard_path"),       (IStringTransform)IStringTransform::DiscardPath);
		TRANSFORMS.RegisterUnit(VariantsCitMod.Identifier("discard_namespace"),  (IStringTransform)IStringTransform::DiscardNamespace);
		TRANSFORMS.RegisterUnit(VariantsCitMod.Identifier("sanitize"),           IStringTransform.SANITIZE_AUTO);
		TRANSFORMS.RegisterUnit(VariantsCitMod.Identifier("sanitize_path"),      IStringTransform.SANITIZE_PATH);
		TRANSFORMS.RegisterUnit(VariantsCitMod.Identifier("sanitize_namespace"), IStringTransform.SANITIZE_NAMESPACE);
		TRANSFORMS.RegisterUnit(VariantsCitMod.Identifier("sanitize_legacy"),    IStringTransform.SANITIZE_LEGACY);
		TRANSFORMS.RegisterUnit(VariantsCitMod.Identifier("sanitize_auto"),      IStringTransform.SANITIZE_AUTO);

		TRANSFORMS.RegisterMap(VariantsCitMod.Identifier("test"),                TestTransform.MAPCODEC);
		TRANSFORMS.RegisterMap(VariantsCitMod.Identifier("matches_any"),         MatchesTransform.MATCHANY_MAPCODEC);
		TRANSFORMS.RegisterMap(VariantsCitMod.Identifier("matches_all"),         MatchesTransform.MATCHALL_MAPCODEC);
		TRANSFORMS.RegisterMap(VariantsCitMod.Identifier("successive"),          SuccessiveTransform.MAPCODEC);
		TRANSFORMS.RegisterMap(VariantsCitMod.Identifier("alternative"),         AlternativeTransform.MAPCODEC);
		TRANSFORMS.RegisterMap(VariantsCitMod.Identifier("foreach"),             ForeachTransform.MAPCODEC);

		TRANSFORMS.RegisterMap(VariantsCitMod.Identifier("whitelist"),           FilterlistTransform.MAPCODEC_WHITELIST);
		TRANSFORMS.RegisterMap(VariantsCitMod.Identifier("blacklist"),           FilterlistTransform.MAPCODEC_BLACKLIST);
		TRANSFORMS.RegisterMap(VariantsCitMod.Identifier("charset_remap"),       CharRemapTransform.MAPCODEC);
		TRANSFORMS.RegisterMap(VariantsCitMod.Identifier("remap"),               RemapTransform.MAPCODEC);
		TRANSFORMS.RegisterMap(VariantsCitMod.Identifier("regex"),               RegexTransform.MAPCODEC);

		TRANSFORMS.RegisterMap(VariantsCitMod.Identifier("equals"),              CodecUtil.MapWithAlternative(StringCompareTransform.MAPCODEC,NumberCompareTransform.MAPCODEC_EQUAL));
		TRANSFORMS.RegisterMap(VariantsCitMod.Identifier("smaller_than"),        NumberCompareTransform.MAPCODEC_SMALLER);
		TRANSFORMS.RegisterMap(VariantsCitMod.Identifier("greater_than"),        NumberCompareTransform.MAPCODEC_GREATER);
		TRANSFORMS.RegisterMap(VariantsCitMod.Identifier("smaller_or_equals"),   NumberCompareTransform.MAPCODEC_GREAT_OR_EQ);
		TRANSFORMS.RegisterMap(VariantsCitMod.Identifier("greater_or_equals"),   NumberCompareTransform.MAPCODEC_SMALL_OR_EQ);

		TRANSFORMS.RegisterMap (VariantsCitMod.Identifier("nbt_path"),            NbtPath.MAPCODEC);
		TRANSFORMS.Register    (VariantsCitMod.Identifier("get_identifier"),      DataConversions.GET_IDENTIFIER_MAPCODEC, DataConversions::StricIdentifier);
		TRANSFORMS.RegisterUnit(VariantsCitMod.Identifier("get_nbt"),             DataConversions::StrictNbt);
		TRANSFORMS.RegisterUnit(VariantsCitMod.Identifier("get_number"),          DataConversions::StrictNumber);
		TRANSFORMS.RegisterUnit(VariantsCitMod.Identifier("get_rich_text"),       DataConversions::StrictRichText);
		TRANSFORMS.RegisterUnit(VariantsCitMod.Identifier("get_rich_text_array"), DataConversions::StrictRichTextArray);
		TRANSFORMS.RegisterUnit(VariantsCitMod.Identifier("get_snbt"),            DataConversions::StrictSnbt);
		TRANSFORMS.RegisterUnit(VariantsCitMod.Identifier("get_string"),          DataConversions::StrictString);


		TRANSFORMS.RegisterMap(VariantsCitMod.Identifier("auto_detect_function"), CodecUtil.MapWithAlternatives(
			RegexTransform.MAPCODEC,
			LogTransform.ANONYMOUS_MAPCODEC,
			NbtPath.MAPCODEC,
			FilterlistTransform.MAPCODEC_BLACKLIST,
			FilterlistTransform.MAPCODEC_WHITELIST,
			MatchesTransform.MATCHANY_MAPCODEC,
			MatchesTransform.MATCHALL_MAPCODEC,
			StringCompareTransform.MAPCODEC,
			NumberCompareTransform.MAPCODEC_EQUAL,
			NumberCompareTransform.MAPCODEC_GREATER,
			NumberCompareTransform.MAPCODEC_SMALLER,
			NumberCompareTransform.MAPCODEC_GREAT_OR_EQ,
			NumberCompareTransform.MAPCODEC_SMALL_OR_EQ
		));


		// ## Preconditions

		PRECONDITIONS.RegisterMap(VariantsCitMod.Identifier("matches_all"), ConditionList.MATCHALL_MAPCODEC);
		PRECONDITIONS.RegisterMap(VariantsCitMod.Identifier("matches_any"), ConditionList.MATCHANY_MAPCODEC);
		PRECONDITIONS.RegisterMap(VariantsCitMod.Identifier("transform"),   ITEM_PROPERTIES.mapCodec);
	}


	static public void RegisterBakedModule(ResourceLocation id, MapCodec<? extends IUnbakedModule> mapcodec){
		MODULES.RegisterMap(id, mapcodec);
	}

	static public void RegisterSimpleModule(ResourceLocation id, MapCodec<? extends IVariantCitModule> mapcodec){
		RegisterBakedModule(id, VariantModuleBaker.Of(mapcodec));
	}

	static public void RegisterSimpleModule(ResourceLocation id, IVariantCitModule unit){
		RegisterSimpleModule(id, MapCodec.unit(unit));
	}

	static public void RegisterLinearModule(ResourceLocation id, MapCodec<? extends ILinearCitModule> mapcodec){
		RegisterBakedModule(id, LinearModuleBaker.Of(mapcodec));
	}

	static public void RegisterLinearModule(ResourceLocation id, ILinearCitModule unit){
		RegisterLinearModule(id, MapCodec.unit(unit));
	}

	static private <T> MapDecoder<T> OptionalParameters(MapDecoder<T> mapcodec){
		return CompoundTag.CODEC
			.optionalFieldOf("parameters", new CompoundTag())
			.flatMap(nbt -> NbtOps.INSTANCE.withParser(mapcodec.decoder()).apply(nbt))
			;
	}

	static private void RegisterRemoved(String oldName, String newName){
		MODULES.Deprecate(
			VariantsCitMod.Identifier(oldName),
			"Module type `"+oldName+"` was removed, use `"+newName+"` instead"
		);
	}
}
