package fr.estecka.variantscit;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.Identifier;
import java.util.Map;
import java.util.Optional;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapDecoder;
import fr.estecka.variantscit.api.ICitModule;
import fr.estecka.variantscit.format.INbtInput;
import fr.estecka.variantscit.format.IStringTransform;
import fr.estecka.variantscit.format.properties.*;
import fr.estecka.variantscit.format.transforms.*;
import fr.estecka.variantscit.modulebakers.*;
import fr.estecka.variantscit.modules.*;
import fr.estecka.variantscit.reload.UnbakedModule;

public final class VCitRegistries
{
	static public final DecodableRegistry<UnbakedModule<?>> MODULES = new DecodableRegistry<>("type", VCitRegistries::OptionalParameters);
	static public final DecodableRegistry<IStringProperty> ITEM_PROPERTIES = new DecodableRegistry<>("property", Identifier.ofVanilla("item_component"), TransformableProperty::CodecOf);
	static public final DecodableRegistry<IStringTransform> TRANSFORMS = new DecodableRegistry<>("function", Identifier.ofVanilla("regex"), OptionalTransform::CodecOf);
	static public final DecodableRegistry<INbtInput> NBT_INPUTS = new DecodableRegistry<>("type");

	static {
		RegisterBakedModule(Identifier.ofVanilla("axolotl_variant"), AxolotlBucketModule.CODEC, AxolotlBucketModule.BAKER);
		RegisterSimpleModule(Identifier.ofVanilla("block_entity_data"), ComponentDataModule.CreateLegacyCodec(DataComponentTypes.BLOCK_ENTITY_DATA));
		RegisterSimpleModule(Identifier.ofVanilla("bucket_entity_data"), ComponentDataModule.CreateLegacyCodec(DataComponentTypes.BUCKET_ENTITY_DATA));
		RegisterSimpleModule(Identifier.ofVanilla("component_data"), ComponentDataModule.CODEC);
		RegisterSimpleModule(Identifier.ofVanilla("component_format"), MultiComponentFormatModule.CODEC);
		RegisterSimpleModule(Identifier.ofVanilla("custom_data"), ComponentDataModule.CreateLegacyCodec(DataComponentTypes.CUSTOM_DATA));
		RegisterSimpleModule(Identifier.ofVanilla("custom_name"), CustomNameModule.CODEC);
		RegisterBakedModule(Identifier.ofVanilla("durability"), DurabilityModule.CODEC, LinearLibrary.GetBaker());
		RegisterSimpleModule(Identifier.ofVanilla("enchantment"), EnchantmentModule.CreateCodec(DataComponentTypes.ENCHANTMENTS));
		RegisterBakedModule(Identifier.ofVanilla("enchantment_vector"), EnchantmentVectorModule.PARAM_MAPCODEC, EnchantmentVectorModule.GetBaker(DataComponentTypes.ENCHANTMENTS));
		RegisterSimpleModule(Identifier.ofVanilla("entity_data"), ComponentDataModule.CreateLegacyCodec(DataComponentTypes.ENTITY_DATA));
		RegisterSimpleModule(Identifier.ofVanilla("instrument"), new GoatHornModule());
		RegisterBakedModule(Identifier.ofVanilla("item_count"), ItemCountModule.CODEC, LinearLibrary.GetBaker());
		RegisterSimpleModule(Identifier.ofVanilla("jukebox_playable"), new MusicDiscModule());
		RegisterSimpleModule(Identifier.ofVanilla("painting_variant"), new PaintingVariantModule());
		RegisterSimpleModule(Identifier.ofVanilla("potion_effect"), new PotionEffectModule());
		RegisterSimpleModule(Identifier.ofVanilla("potion_type"), new PotionTypeModule());
		RegisterSimpleModule(Identifier.ofVanilla("stored_enchantment"), EnchantmentModule.CreateCodec(DataComponentTypes.STORED_ENCHANTMENTS));
		RegisterSimpleModule(Identifier.ofVanilla("stored_enchantments"), CodecUtil.WithWarning(
			MapCodec.unit(new EnchantmentModule(DataComponentTypes.STORED_ENCHANTMENTS, Map.of(), Optional.empty())),
			"Module name `stored_enchantments` (plural) is being deprecated. use `stored_enchantment` (singular) instead."
		));
		RegisterBakedModule(Identifier.ofVanilla("stored_enchantment_vector"), EnchantmentVectorModule.PARAM_MAPCODEC, EnchantmentVectorModule.GetBaker(DataComponentTypes.STORED_ENCHANTMENTS));
		RegisterSimpleModule(Identifier.ofVanilla("trim"), new TrimModule());
		RegisterSimpleModule(Identifier.ofVanilla("trim_pattern"), new TrimPatternModule());
		RegisterSimpleModule(Identifier.ofVanilla("trim_material"), new TrimPatternModule());

		ITEM_PROPERTIES.RegisterUnit(Identifier.ofVanilla("axolotl_variant"), AxolotlVariantProperty.UNIT);
		ITEM_PROPERTIES.Register(Identifier.ofVanilla("bucket_entity_age"), EntityAgeMapProperty.MAP_CODEC, EntityAgeMapProperty.UNIT);
		ITEM_PROPERTIES.RegisterMap(Identifier.ofVanilla("item_component"), ItemComponentProperty.MAP_CODEC);
		ITEM_PROPERTIES.RegisterUnit(Identifier.ofVanilla("item_count"), new ItemCountProperty());
		ITEM_PROPERTIES.RegisterUnit(Identifier.ofVanilla("item_type"), new ItemTypeProperty());
		ITEM_PROPERTIES.RegisterUnit(Identifier.ofVanilla("painting_variant"), PaintingVariantProperty.UNIT);

		TRANSFORMS.RegisterUnit(Identifier.ofVanilla("noop"),               IStringTransform.NOOP);
		TRANSFORMS.RegisterUnit(Identifier.ofVanilla("null"),               IStringTransform.NULL);
		TRANSFORMS.RegisterUnit(Identifier.ofVanilla("lowercase"),          String::toLowerCase);
		TRANSFORMS.RegisterUnit(Identifier.ofVanilla("discard_path"),       IStringTransform::DiscardPath);
		TRANSFORMS.RegisterUnit(Identifier.ofVanilla("discard_namespace"),  IStringTransform::DiscardNamespace);
		TRANSFORMS.RegisterUnit(Identifier.ofVanilla("sanitize"),           IStringTransform.SANITIZE);
		TRANSFORMS.RegisterUnit(Identifier.ofVanilla("sanitize_path"),      IStringTransform.SANITIZE_PATH);
		TRANSFORMS.RegisterUnit(Identifier.ofVanilla("sanitize_namespace"), IStringTransform.SANITIZE_NAMESPACE);
		TRANSFORMS.RegisterUnit(Identifier.ofVanilla("sanitize_auto"),      IStringTransform::AutoSanitize);
		TRANSFORMS.RegisterMap(Identifier.ofVanilla("test"),                TestTransform.MAPCODEC);
		TRANSFORMS.RegisterMap(Identifier.ofVanilla("successive"),          SuccessiveTransform.MAPCODEC);
		TRANSFORMS.RegisterMap(Identifier.ofVanilla("alternative"),         AlternativeTransform.MAPCODEC);
		TRANSFORMS.RegisterMap(Identifier.ofVanilla("whitelist"),           FilterlistTransform.MAPCODEC_WHITELIST);
		TRANSFORMS.RegisterMap(Identifier.ofVanilla("blacklist"),           FilterlistTransform.MAPCODEC_BLACKLIST);
		TRANSFORMS.RegisterMap(Identifier.ofVanilla("charset_remap"),       CharRemapTransform.MAPCODEC);
		TRANSFORMS.RegisterMap(Identifier.ofVanilla("remap"),               RemapTransform.MAPCODEC);
		TRANSFORMS.RegisterMap(Identifier.ofVanilla("regex"),               RegexTransform.MAPCODEC);

		NBT_INPUTS.RegisterUnit(Identifier.ofVanilla("auto"),            INbtInput.AUTO);
		NBT_INPUTS.RegisterUnit(Identifier.ofVanilla("primitive"),       INbtInput.PRIMITIVE);
		NBT_INPUTS.RegisterUnit(Identifier.ofVanilla("string"),          INbtInput::String);
		NBT_INPUTS.RegisterUnit(Identifier.ofVanilla("number"),          INbtInput::Number);
		NBT_INPUTS.RegisterUnit(Identifier.ofVanilla("identifier"),      INbtInput::Identifier);
		NBT_INPUTS.RegisterUnit(Identifier.ofVanilla("rich_text"),       INbtInput::RichText);
		NBT_INPUTS.RegisterUnit(Identifier.ofVanilla("rich_text_array"), INbtInput::RichTextArray);
	}

	static public <T> void RegisterBakedModule(Identifier id, MapCodec<T> mapcodec, IModuleBaker<T> baker){
		MODULES.RegisterMap(id, mapcodec.xmap(
			parameters -> new UnbakedModule<>(baker, parameters),
			UnbakedModule::parameters
		));
	}

	static public void RegisterSimpleModule(Identifier id, MapCodec<? extends ICitModule> mapcodec){
		RegisterBakedModule(id, mapcodec, VariantLibrary::Bake);
	}

	static public void RegisterSimpleModule(Identifier id, ICitModule unit){
		RegisterSimpleModule(id, MapCodec.unit(unit));
	}

	static private <T> MapDecoder<T> OptionalParameters(MapDecoder<T> mapcodec){
		return NbtCompound.CODEC
			.optionalFieldOf("parameters", new NbtCompound())
			.flatMap(nbt -> NbtOps.INSTANCE.withParser(mapcodec.decoder()).apply(nbt))
			;
	}
}
