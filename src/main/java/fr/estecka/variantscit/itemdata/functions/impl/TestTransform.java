package fr.estecka.variantscit.itemdata.functions.impl;

import com.mojang.serialization.MapCodec;
import fr.estecka.variantscit.itemdata.containers.IDataContainer;
import fr.estecka.variantscit.itemdata.functions.IDataFunction;
import fr.estecka.variantscit.itemdata.functions.SuccessiveTransform;

public record TestTransform(IDataFunction subTransform)
implements IDataFunction
{
	static public final MapCodec<TestTransform> MAPCODEC = SuccessiveTransform.CODEC
		.fieldOf("tester")
		.xmap(TestTransform::new, TestTransform::subTransform)
		;

	@Override
	public IDataContainer LooseTypedTransform(IDataContainer original) {
		var r = subTransform.LooseTypedTransform(original);
		return (r == null) ? null : original;
	}
}
