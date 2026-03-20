package fr.estecka.variantscit.itemdata.transforms;

import com.mojang.serialization.Codec;
import fr.estecka.variantscit.VCitRegistries;
import fr.estecka.variantscit.itemdata.containers.IDataContainer;

@FunctionalInterface
public interface IDataTransform
{
	static public final Codec<IDataTransform> CODEC = VCitRegistries.TRANSFORMS.codec;
	static public final IDataTransform NOOP = o->o;

	IDataContainer LooseTypedTransform(IDataContainer input);
}
