package fr.estecka.variantscit.format.properties;

import fr.estecka.variantscit.DecodableRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public interface IStringProperty
{
	static public final DecodableRegistry<IStringProperty> REGISTRY = new DecodableRegistry<>("property", TransformableProperty::CodecOf){{
		this.RegisterUnit(Identifier.ofVanilla("axolotl_variant"), AxolotlVariantProperty.UNIT);
		this.Register(Identifier.ofVanilla("bucket_entity_age"), EntityAgeMapProperty.MAP_CODEC, EntityAgeMapProperty.UNIT);
		this.RegisterMap(Identifier.ofVanilla("item_component"), ItemComponentProperty.MAP_CODEC);
		this.RegisterUnit(Identifier.ofVanilla("item_count"), new ItemCountProperty());
		this.RegisterUnit(Identifier.ofVanilla("item_type"), new ItemTypeProperty());
	}};

	int GetPropertyHash(ItemStack stack);
	String GetPropertyString(ItemStack stack);
	Object GetReference(ItemStack stack);
}
