package fr.estecka.variantscit.format.properties;

import fr.estecka.variantscit.DecodableRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public interface IStringProperty
{
	static public final DecodableRegistry<IStringProperty> REGISTRY = new DecodableRegistry<>("property"){{
		this.Register(Identifier.ofVanilla("item_type"), ItemTypeProperty.CODEC);
		this.Register(Identifier.ofVanilla("item_count"), new ItemCountProperty());
		this.Register(Identifier.ofVanilla("item_component"), ItemComponentProperty.CODEC);
	}};

	int GetPropertyHash(ItemStack stack);
	String GetPropertyString(ItemStack stack);
	Object GetReference(ItemStack stack);
}
