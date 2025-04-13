package fr.estecka.variantscit;

import net.minecraft.util.Identifier;
import fr.estecka.variantscit.format.properties.*;

public final class VCitRegistries
{
	static public final DecodableRegistry<IStringProperty> ITEM_PROPERTIES = new DecodableRegistry<>("property", TransformableProperty::CodecOf);

	static {
		ITEM_PROPERTIES.RegisterUnit(Identifier.ofVanilla("axolotl_variant"), AxolotlVariantProperty.UNIT);
		ITEM_PROPERTIES.Register(Identifier.ofVanilla("bucket_entity_age"), EntityAgeMapProperty.MAP_CODEC, EntityAgeMapProperty.UNIT);
		ITEM_PROPERTIES.RegisterMap(Identifier.ofVanilla("item_component"), ItemComponentProperty.MAP_CODEC);
		ITEM_PROPERTIES.RegisterUnit(Identifier.ofVanilla("item_count"), new ItemCountProperty());
		ITEM_PROPERTIES.RegisterUnit(Identifier.ofVanilla("item_type"), new ItemTypeProperty());
		ITEM_PROPERTIES.RegisterUnit(Identifier.ofVanilla("painting_variant"), PaintingVariantProperty.UNIT);
	}
}
