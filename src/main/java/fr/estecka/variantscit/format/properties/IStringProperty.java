package fr.estecka.variantscit.format.properties;

import fr.estecka.variantscit.DecodableRegistry;
import fr.estecka.variantscit.format.ETransform;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public interface IStringProperty
{
	static public final DecodableRegistry<IStringProperty> REGISTRY = new DecodableRegistry<>("property"){{
		this.Register(Identifier.ofVanilla("item_type"), ItemTypeProperty.MAP_CODEC, new ItemTypeProperty(new ETransform[0]));
		this.Register(Identifier.ofVanilla("item_count"), new ItemCountProperty());
		this.Register(Identifier.ofVanilla("item_component"), ItemComponentProperty.MAP_CODEC);
	}};

	int GetPropertyHash(ItemStack stack);
	String GetPropertyString(ItemStack stack);
	Object GetReference(ItemStack stack);
}
