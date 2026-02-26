package fr.estecka.variantscit.itemdata.transforms.impl;

import java.util.List;
import com.mojang.serialization.MapCodec;
import fr.estecka.variantscit.itemdata.containers.IDataContainer;
import fr.estecka.variantscit.itemdata.transforms.IDataTransform;
import fr.estecka.variantscit.itemdata.transforms.SuccessiveTransform;


public record AlternativeTransform(List<IDataTransform> alternatives)
implements IDataTransform
{
	static public final MapCodec<AlternativeTransform> MAPCODEC = SuccessiveTransform.CODEC.listOf().fieldOf("alternatives")
		.xmap(AlternativeTransform::new, AlternativeTransform::alternatives)
		;

	@Override
	public IDataContainer LooseTypedTransform(IDataContainer input) {
		for (IDataTransform t : alternatives) {
			IDataContainer result = t.LooseTypedTransform(input);
			if (result != null) return result;
		}

		return null;
	}
}
