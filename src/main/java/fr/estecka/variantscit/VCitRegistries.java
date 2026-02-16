package fr.estecka.variantscit;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import java.util.Map;
import java.util.Optional;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapDecoder;
import fr.estecka.variantscit.modules.libraries.IVariantCitModule;
import fr.estecka.variantscit.format.INbtInput;
import fr.estecka.variantscit.format.IStringTransform;
import fr.estecka.variantscit.format.properties.*;
import fr.estecka.variantscit.format.transforms.*;
import fr.estecka.variantscit.modules.*;
import fr.estecka.variantscit.modules.impl.*;
import fr.estecka.variantscit.modules.libraries.*;
import fr.estecka.variantscit.reload.UnbakedModule;

public final class VCitRegistries
{
	static public final DecodableRegistry<UnbakedModule<?>> MODULES = new DecodableRegistry<>("type", VCitRegistries::OptionalParameters);
	static public final DecodableRegistry<IStringProperty> ITEM_PROPERTIES = new DecodableRegistry<>("property", ResourceLocation.withDefaultNamespace("item_component"), TransformableProperty::CodecOf);
	static public final DecodableRegistry<IStringTransform> TRANSFORMS = new DecodableRegistry<>("function", ResourceLocation.withDefaultNamespace("regex"), OptionalTransform::CodecOf);
	static public final DecodableRegistry<INbtInput> NBT_INPUTS = new DecodableRegistry<>("type");

	static {
		RegisterBakedModule (ResourceLocation.withDefaultNamespace("axolotl_variant"), AxolotlBucketModule.CODEC, AxolotlBucketModule.BAKER);
		RegisterSimpleModule(ResourceLocation.withDefaultNamespace("block_entity_data"), ComponentDataModule.CreateLegacyCodec(DataComponents.BLOCK_ENTITY_DATA));
		RegisterSimpleModule(ResourceLocation.withDefaultNamespace("bucket_entity_data"), ComponentDataModule.CreateLegacyCodec(DataComponents.BUCKET_ENTITY_DATA));
		RegisterSimpleModule(ResourceLocation.withDefaultNamespace("component_data"), ComponentDataModule.CODEC);
		RegisterSimpleModule(ResourceLocation.withDefaultNamespace("component_format"), MultiComponentFormatModule.CODEC);
		RegisterBakedModule (ResourceLocation.withDefaultNamespace("component_threshold"), ComponentThresholdModule.MAPCODEC, LinearLibrary.GetBaker());
		RegisterSimpleModule(ResourceLocation.withDefaultNamespace("custom_data"), ComponentDataModule.CreateLegacyCodec(DataComponents.CUSTOM_DATA));
		RegisterSimpleModule(ResourceLocation.withDefaultNamespace("custom_name"), CustomNameModule.CODEC);
		RegisterBakedModule (ResourceLocation.withDefaultNamespace("durability"), DurabilityModule.CODEC, LinearLibrary.GetBaker());
		RegisterSimpleModule(ResourceLocation.withDefaultNamespace("enchantment"), EnchantmentModule.CreateCodec(DataComponents.ENCHANTMENTS));
		RegisterBakedModule (ResourceLocation.withDefaultNamespace("enchantment_vector"), EnchantmentVectorModule.PARAM_MAPCODEC, EnchantmentVectorModule.GetBaker(DataComponents.ENCHANTMENTS));
		RegisterSimpleModule(ResourceLocation.withDefaultNamespace("entity_data"), ComponentDataModule.CreateLegacyCodec(DataComponents.ENTITY_DATA));
		RegisterSimpleModule(ResourceLocation.withDefaultNamespace("instrument"), new GoatHornModule());
		RegisterBakedModule (ResourceLocation.withDefaultNamespace("item_count"), ItemCountModule.CODEC, LinearLibrary.GetBaker());
		RegisterSimpleModule(ResourceLocation.withDefaultNamespace("jukebox_playable"), new MusicDiscModule());
		RegisterSimpleModule(ResourceLocation.withDefaultNamespace("painting_variant"), new PaintingVariantModule());
		RegisterSimpleModule(ResourceLocation.withDefaultNamespace("potion_effect"), new PotionEffectModule());
		RegisterSimpleModule(ResourceLocation.withDefaultNamespace("potion_type"), new PotionTypeModule());
		RegisterSimpleModule(ResourceLocation.withDefaultNamespace("stored_enchantment"), EnchantmentModule.CreateCodec(DataComponents.STORED_ENCHANTMENTS));
		RegisterSimpleModule(ResourceLocation.withDefaultNamespace("stored_enchantments"), CodecUtil.WithWarning(
			MapCodec.unit(new EnchantmentModule(DataComponents.STORED_ENCHANTMENTS, Map.of(), Optional.empty())),
			"Module name `stored_enchantments` (plural) is being deprecated. use `stored_enchantment` (singular) instead."
		));
		RegisterBakedModule(ResourceLocation.withDefaultNamespace("stored_enchantment_vector"), EnchantmentVectorModule.PARAM_MAPCODEC, EnchantmentVectorModule.GetBaker(DataComponents.STORED_ENCHANTMENTS));
		RegisterSimpleModule(ResourceLocation.withDefaultNamespace("trim"), new TrimModule());
		RegisterSimpleModule(ResourceLocation.withDefaultNamespace("trim_pattern"), new TrimPatternModule());
		RegisterSimpleModule(ResourceLocation.withDefaultNamespace("trim_material"), new TrimPatternModule());

		ITEM_PROPERTIES.RegisterUnit(ResourceLocation.withDefaultNamespace("axolotl_variant"), AxolotlVariantProperty.UNIT);
		ITEM_PROPERTIES.Register(ResourceLocation.withDefaultNamespace("bucket_entity_age"), EntityAgeMapProperty.MAP_CODEC, EntityAgeMapProperty.UNIT);
		ITEM_PROPERTIES.RegisterMap(ResourceLocation.withDefaultNamespace("item_component"), ItemComponentProperty.MAP_CODEC);
		ITEM_PROPERTIES.RegisterUnit(ResourceLocation.withDefaultNamespace("item_count"), new ItemCountProperty());
		ITEM_PROPERTIES.RegisterUnit(ResourceLocation.withDefaultNamespace("item_type"), new ItemTypeProperty());
		ITEM_PROPERTIES.RegisterUnit(ResourceLocation.withDefaultNamespace("painting_variant"), PaintingVariantProperty.UNIT);

		TRANSFORMS.RegisterUnit(ResourceLocation.withDefaultNamespace("noop"),               IStringTransform.NOOP);
		TRANSFORMS.RegisterUnit(ResourceLocation.withDefaultNamespace("null"),               IStringTransform.NULL);
		TRANSFORMS.RegisterUnit(ResourceLocation.withDefaultNamespace("lowercase"),          String::toLowerCase);
		TRANSFORMS.RegisterUnit(ResourceLocation.withDefaultNamespace("discard_path"),       IStringTransform::DiscardPath);
		TRANSFORMS.RegisterUnit(ResourceLocation.withDefaultNamespace("discard_namespace"),  IStringTransform::DiscardNamespace);
		TRANSFORMS.RegisterUnit(ResourceLocation.withDefaultNamespace("sanitize"),           IStringTransform.SANITIZE);
		TRANSFORMS.RegisterUnit(ResourceLocation.withDefaultNamespace("sanitize_path"),      IStringTransform.SANITIZE_PATH);
		TRANSFORMS.RegisterUnit(ResourceLocation.withDefaultNamespace("sanitize_namespace"), IStringTransform.SANITIZE_NAMESPACE);
		TRANSFORMS.RegisterUnit(ResourceLocation.withDefaultNamespace("sanitize_auto"),      IStringTransform::AutoSanitize);
		TRANSFORMS.RegisterMap(ResourceLocation.withDefaultNamespace("test"),                TestTransform.MAPCODEC);
		TRANSFORMS.RegisterMap(ResourceLocation.withDefaultNamespace("successive"),          SuccessiveTransform.MAPCODEC);
		TRANSFORMS.RegisterMap(ResourceLocation.withDefaultNamespace("alternative"),         AlternativeTransform.MAPCODEC);
		TRANSFORMS.RegisterMap(ResourceLocation.withDefaultNamespace("whitelist"),           FilterlistTransform.MAPCODEC_WHITELIST);
		TRANSFORMS.RegisterMap(ResourceLocation.withDefaultNamespace("blacklist"),           FilterlistTransform.MAPCODEC_BLACKLIST);
		TRANSFORMS.RegisterMap(ResourceLocation.withDefaultNamespace("charset_remap"),       CharRemapTransform.MAPCODEC);
		TRANSFORMS.RegisterMap(ResourceLocation.withDefaultNamespace("remap"),               RemapTransform.MAPCODEC);
		TRANSFORMS.RegisterMap(ResourceLocation.withDefaultNamespace("regex"),               RegexTransform.MAPCODEC);

		NBT_INPUTS.RegisterUnit(ResourceLocation.withDefaultNamespace("auto"),            INbtInput.AUTO);
		NBT_INPUTS.RegisterUnit(ResourceLocation.withDefaultNamespace("primitive"),       INbtInput.PRIMITIVE);
		NBT_INPUTS.RegisterUnit(ResourceLocation.withDefaultNamespace("string"),          INbtInput::String);
		NBT_INPUTS.RegisterUnit(ResourceLocation.withDefaultNamespace("number"),          INbtInput::Number);
		NBT_INPUTS.RegisterUnit(ResourceLocation.withDefaultNamespace("identifier"),      INbtInput::Identifier);
		NBT_INPUTS.RegisterUnit(ResourceLocation.withDefaultNamespace("rich_text"),       INbtInput::RichText);
		NBT_INPUTS.RegisterUnit(ResourceLocation.withDefaultNamespace("rich_text_array"), INbtInput::RichTextArray);
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
}
