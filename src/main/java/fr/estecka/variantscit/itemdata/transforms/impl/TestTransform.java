package fr.estecka.variantscit.itemdata.transforms.impl;

import com.mojang.serialization.MapCodec;
import fr.estecka.variantscit.util.CodecUtil;
import fr.estecka.variantscit.itemdata.containers.IDataContainer;
import fr.estecka.variantscit.itemdata.transforms.IDataTransform;
import fr.estecka.variantscit.itemdata.transforms.SuccessiveTransform;

public record TestTransform(IDataTransform subTransform)
implements IDataTransform
{
	static public final MapCodec<TestTransform> MAPCODEC = CodecUtil.WithAlias(SuccessiveTransform.CODEC, "test", "tester")
		.xmap(TestTransform::new, TestTransform::subTransform)
		;

	@Override
	public IDataContainer LooseTypedTransform(IDataContainer original) {
		var r = subTransform.LooseTypedTransform(original);
		return (r == null) ? null : original;
	}
}
