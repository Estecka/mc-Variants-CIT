package fr.estecka.variantscit.itemdata.functions;

import com.mojang.serialization.Codec;
import fr.estecka.variantscit.VCitRegistries;
import fr.estecka.variantscit.itemdata.containers.IDataContainer;

@FunctionalInterface
public interface IDataFunction
{
	static public final Codec<IDataFunction> CODEC = VCitRegistries.DATA_FUNCTION.codec;
	static public final IDataFunction NOOP = o->o;

	IDataContainer LooseTypedTransform(IDataContainer input);
}
