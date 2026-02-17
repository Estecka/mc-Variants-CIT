package fr.estecka.variantscit.format.properties;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import fr.estecka.variantscit.VCitRegistries;
import net.minecraft.world.item.ItemStack;
import fr.estecka.variantscit.modules.cache.ICacheKey;

public interface IStringProperty
extends ICacheKey.Keyable
{
	static public MapCodec<IStringProperty> MAP_CODEC = VCitRegistries.ITEM_PROPERTIES.mapCodec;
	static public Codec<IStringProperty> CODEC = Codec.withAlternative(
		VCitRegistries.ITEM_PROPERTIES.codec,
		ItemComponentProperty.MONOSTRING_DECODER
	);

	/**
	 * @return The value  of the property, to be used  in a  variant id. At this
	 * point the property is not required to actually be a valid identifier.
	 */
	String GetPropertyString(ItemStack stack);
}
