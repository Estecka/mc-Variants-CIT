package fr.estecka.variantscit.format.transforms;

import java.util.List;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import fr.estecka.variantscit.CodecUtil;
import fr.estecka.variantscit.format.IStringTransform;

public record SuccessiveTransform(IStringTransform[] subTransforms)
implements IStringTransform
{
	static public final Codec<IStringTransform> CODEC = CodecUtil.OneOrMany(IStringTransform.CODEC)
		.xmap(SuccessiveTransform::Wrap, SuccessiveTransform::Unwrap)
		;

	static public final MapCodec<IStringTransform> MAPCODEC = IStringTransform.CODEC.listOf()
		.fieldOf("transform")
		.xmap(SuccessiveTransform::Wrap, SuccessiveTransform::Unwrap)
		;

	static public List<IStringTransform> Unwrap(IStringTransform t){
		if (t instanceof SuccessiveTransform succ)
			return succ.SubTransformList();
		else
			return List.of(t);
	}

	static public IStringTransform Wrap(List<IStringTransform> sub){
		if (sub.size() == 0)
			return o->o;
		else if (sub.size() == 1)
			return sub.get(0);
		else
			return new SuccessiveTransform(sub);
	}


	public SuccessiveTransform(List<IStringTransform> subTransforms){
		this(subTransforms.toArray(IStringTransform[]::new));
	}

	public List<IStringTransform> SubTransformList(){
		return List.<IStringTransform>of(this.subTransforms);
	}

	@Override
	public String apply(String input) {
		for (IStringTransform t : subTransforms) {
			input = t.apply(input);
			if (input == null)
				return null;
		}
		return input;
	}
}
