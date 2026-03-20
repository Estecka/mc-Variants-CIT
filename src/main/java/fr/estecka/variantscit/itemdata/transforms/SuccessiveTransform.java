package fr.estecka.variantscit.itemdata.transforms;

import java.util.List;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import fr.estecka.variantscit.CodecUtil;
import fr.estecka.variantscit.itemdata.containers.IDataContainer;

public record SuccessiveTransform(IDataTransform[] subTransforms)
implements IDataTransform
{
	static public final Codec<IDataTransform> CODEC = CodecUtil.OneOrMany(IDataTransform.CODEC)
		.xmap(SuccessiveTransform::Wrap, SuccessiveTransform::Unwrap)
		;

	static public final MapCodec<IDataTransform> MAPCODEC = IDataTransform.CODEC.listOf()
		.fieldOf("chain")
		.xmap(SuccessiveTransform::Wrap, SuccessiveTransform::Unwrap)
		;

	static public List<IDataTransform> Unwrap(IDataTransform t){
		if (t instanceof SuccessiveTransform succ)
			return succ.SubTransformList();
		else
			return List.of(t);
	}

	static public IDataTransform Wrap(List<IDataTransform> sub){
		if (sub.size() == 0)
			return IDataTransform.NOOP;
		else if (sub.size() == 1)
			return sub.get(0);
		else
			return new SuccessiveTransform(sub);
	}


	public SuccessiveTransform(List<IDataTransform> subTransforms){
		this(subTransforms.toArray(IDataTransform[]::new));
	}

	public List<IDataTransform> SubTransformList(){
		return List.<IDataTransform>of(this.subTransforms);
	}

	@Override
	public IDataContainer LooseTypedTransform(IDataContainer input) {
		for (IDataTransform t : subTransforms) {
			input = t.LooseTypedTransform(input);
			if (input == null)
				return null;
		}
		return input;
	}
}
