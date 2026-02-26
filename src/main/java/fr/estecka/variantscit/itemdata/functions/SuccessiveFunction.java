package fr.estecka.variantscit.itemdata.functions;

import java.util.List;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import fr.estecka.variantscit.CodecUtil;
import fr.estecka.variantscit.itemdata.containers.IDataContainer;

public record SuccessiveFunction(IDataFunction[] subTransforms)
implements IDataFunction
{
	static public final Codec<IDataFunction> CODEC = CodecUtil.OneOrMany(IDataFunction.CODEC)
		.xmap(SuccessiveFunction::Wrap, SuccessiveFunction::Unwrap)
		;

	static public final MapCodec<IDataFunction> MAPCODEC = IDataFunction.CODEC.listOf()
		.fieldOf("chain")
		.xmap(SuccessiveFunction::Wrap, SuccessiveFunction::Unwrap)
		;

	static public List<IDataFunction> Unwrap(IDataFunction t){
		if (t instanceof SuccessiveFunction succ)
			return succ.SubTransformList();
		else
			return List.of(t);
	}

	static public IDataFunction Wrap(List<IDataFunction> sub){
		if (sub.size() == 0)
			return IDataFunction.NOOP;
		else if (sub.size() == 1)
			return sub.get(0);
		else
			return new SuccessiveFunction(sub);
	}


	public SuccessiveFunction(List<IDataFunction> subTransforms){
		this(subTransforms.toArray(IDataFunction[]::new));
	}

	public List<IDataFunction> SubTransformList(){
		return List.<IDataFunction>of(this.subTransforms);
	}

	@Override
	public IDataContainer LooseTypedTransform(IDataContainer result) {
		for (IDataFunction t : subTransforms) {
			result = t.LooseTypedTransform(result);
			if (result == null)
				return null;
		}
		return result;
	}
}
