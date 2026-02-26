package fr.estecka.variantscit.itemdata.functions.impl;

import java.util.List;
import com.mojang.serialization.MapCodec;
import fr.estecka.variantscit.itemdata.containers.IDataContainer;
import fr.estecka.variantscit.itemdata.functions.IDataFunction;
import fr.estecka.variantscit.itemdata.functions.SuccessiveTransform;


public record AlternativeTransform(List<IDataFunction> alternatives)
implements IDataFunction
{
	static public final MapCodec<AlternativeTransform> MAPCODEC = SuccessiveTransform.CODEC.listOf().fieldOf("alternatives")
		.xmap(AlternativeTransform::new, AlternativeTransform::alternatives)
		;

	@Override
	public IDataContainer LooseTypedTransform(IDataContainer input) {
		for (IDataFunction t : alternatives) {
			IDataContainer result = t.LooseTypedTransform(input);
			if (result != null) return result;
		}

		return null;
	}
}
