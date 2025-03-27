package fr.estecka.variantscit.format.properties;

import fr.estecka.variantscit.DecodableRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public interface IStringProperty
{
	static public final DecodableRegistry<IStringProperty> REGISTRY = new DecodableRegistry<>("property", TransformableProperty::CodecOf){{
		this.Register(Identifier.ofVanilla("axolotl_variant"), AxolotlVariantProperty.UNIT);
		this.Register(Identifier.ofVanilla("bucket_entity_age"), EntityAgeMapProperty.MAP_CODEC, new EntityAgeMapProperty("", "_baby"));
		this.Register(Identifier.ofVanilla("item_component"), ItemComponentProperty.MAP_CODEC);
		this.Register(Identifier.ofVanilla("item_count"), new ItemCountProperty());
		this.Register(Identifier.ofVanilla("item_type"), new ItemTypeProperty());
	}};

	int GetPropertyHash(ItemStack stack);
	String GetPropertyString(ItemStack stack);
	Object GetReference(ItemStack stack);
}
