package fr.estecka.variantscit.itemdata.transforms;

import com.mojang.serialization.Codec;
import fr.estecka.variantscit.VCitRegistries;
import fr.estecka.variantscit.itemdata.containers.IDataContainer;
import fr.estecka.variantscit.itemdata.containers.RawDataContainer;

@FunctionalInterface
public interface IDataTransform
{
	static public final Codec<IDataTransform> CODEC = VCitRegistries.TRANSFORMS.codec;
	static public final IDataTransform NOOP = o->o;

	IDataContainer LooseTypedTransform(IDataContainer input);

	static boolean Test(IDataTransform transform, Object data){
		return transform.LooseTypedTransform(RawDataContainer.OfNullable(data)) != null;
	}
}
