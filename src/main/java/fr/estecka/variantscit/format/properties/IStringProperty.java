package fr.estecka.variantscit.format.properties;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import fr.estecka.variantscit.CodecUtil;
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

	static public MapCodec<IStringProperty> MAP_CODEC = CodecUtil.MapWithAlternative(REGISTRY.mapCodec, TransformableProperty.CodecOf(ItemComponentProperty.MAP_CODEC));
	static public Codec<IStringProperty> CODEC = MAP_CODEC.codec();

	int GetPropertyHash(ItemStack stack);
	String GetPropertyString(ItemStack stack);
	Object GetReference(ItemStack stack);
}
