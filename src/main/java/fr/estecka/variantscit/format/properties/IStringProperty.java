package fr.estecka.variantscit.format.properties;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import fr.estecka.variantscit.CodecUtil;
import fr.estecka.variantscit.VCitRegistries;
import net.minecraft.item.ItemStack;

public interface IStringProperty
{
	static public MapCodec<IStringProperty> MAP_CODEC = CodecUtil.MapWithAlternative(VCitRegistries.ITEM_PROPERTIES.mapCodec, TransformableProperty.CodecOf(ItemComponentProperty.MAP_CODEC));
	static public Codec<IStringProperty> CODEC = Codec.withAlternative(VCitRegistries.ITEM_PROPERTIES.unitCodec, MAP_CODEC.codec());
	/**
	 * Used  for  caching  the variant IDs  associated  with  a given  property.
	 * Typically the hash of {@link #GetReference}.
	 * 
	 * A given  hash can  only be  associated  with a  single  value  of  {@link
	 * #GetPropertyString}. On the  other  hand, the  same  string value  can be
	 * associated with multiple different hashes in cases when the encompassing
	 * component has changed elsewhere.
	 */
	int GetPropertyHash(ItemStack stack);

	/**
	 * A direct reference  to the property  or its encompassing object. Used for
	 * clearing caches whenever a property is no longer used anywhere.
	 * 
	 * The  returned  object  should  be  immutable. Like  for  hashes, a  given
	 * identity  must always  correspond  to the  same  return  value of  {@link
	 * #GetPropertyString}, but  the  same  string  value  is allowed  to  match
	 * multiple identities.
	 */
	Object GetReference(ItemStack stack);

	/**
	 * @return The value  of the property, to be used  in a  variant id. At this
	 * point the property is not required to actually be a valid identifier.
	 */
	String GetPropertyString(ItemStack stack);
}
