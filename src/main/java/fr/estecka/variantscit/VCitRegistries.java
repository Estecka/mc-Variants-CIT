package fr.estecka.variantscit;

import net.minecraft.util.Identifier;
import fr.estecka.variantscit.format.INbtInput;
import fr.estecka.variantscit.format.IStringTransform;
import fr.estecka.variantscit.format.properties.*;
import fr.estecka.variantscit.format.transforms.*;

public final class VCitRegistries
{
	static public final DecodableRegistry<IStringProperty> ITEM_PROPERTIES = new DecodableRegistry<>("property", Identifier.ofVanilla("item_component"), TransformableProperty::CodecOf);
	static public final DecodableRegistry<IStringTransform> TRANSFORMS = new DecodableRegistry<>("type", Identifier.ofVanilla("regex"), OptionalTransform::CodecOf);
	static public final DecodableRegistry<INbtInput> NBT_INPUTS = new DecodableRegistry<>("type");

	static {
		ITEM_PROPERTIES.RegisterUnit(Identifier.ofVanilla("axolotl_variant"), AxolotlVariantProperty.UNIT);
		ITEM_PROPERTIES.Register(Identifier.ofVanilla("bucket_entity_age"), EntityAgeMapProperty.MAP_CODEC, EntityAgeMapProperty.UNIT);
		ITEM_PROPERTIES.RegisterMap(Identifier.ofVanilla("item_component"), ItemComponentProperty.MAP_CODEC);
		ITEM_PROPERTIES.RegisterUnit(Identifier.ofVanilla("item_count"), new ItemCountProperty());
		ITEM_PROPERTIES.RegisterUnit(Identifier.ofVanilla("item_type"), new ItemTypeProperty());
		ITEM_PROPERTIES.RegisterUnit(Identifier.ofVanilla("painting_variant"), PaintingVariantProperty.UNIT);

		TRANSFORMS.RegisterUnit(Identifier.ofVanilla("noop"),               s->s);
		TRANSFORMS.RegisterUnit(Identifier.ofVanilla("lowercase"),          String::toLowerCase);
		TRANSFORMS.RegisterUnit(Identifier.ofVanilla("discard_path"),       IStringTransform::DiscardPath);
		TRANSFORMS.RegisterUnit(Identifier.ofVanilla("discard_namespace"),  IStringTransform::DiscardNamespace);
		TRANSFORMS.RegisterUnit(Identifier.ofVanilla("sanitize"),           IStringTransform.SANITIZE);
		TRANSFORMS.RegisterUnit(Identifier.ofVanilla("sanitize_path"),      IStringTransform.SANITIZE_PATH);
		TRANSFORMS.RegisterUnit(Identifier.ofVanilla("sanitize_namespace"), IStringTransform.SANITIZE_NAMESPACE);
		TRANSFORMS.RegisterUnit(Identifier.ofVanilla("sanitize_auto"),      IStringTransform::AutoSanitize);
		TRANSFORMS.RegisterMap(Identifier.ofVanilla("successive"),          SuccessiveTransform.MAPCODEC);
		TRANSFORMS.RegisterMap(Identifier.ofVanilla("alternative"),         AlternativeTransform.MAPCODEC);
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
}
