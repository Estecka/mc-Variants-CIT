package fr.estecka.variantscit.format.transforms;

import java.util.List;
import com.mojang.serialization.MapCodec;
import fr.estecka.variantscit.format.IStringTransform;

public record AlternativeTransform(List<IStringTransform> alternatives)
implements IStringTransform
{
	static public final MapCodec<AlternativeTransform> MAPCODEC = SuccessiveTransform.CODEC.listOf().fieldOf("alternatives")
		.xmap(AlternativeTransform::new, AlternativeTransform::alternatives)
		;

	@Override
	public String apply(String input) {
		for (IStringTransform t : alternatives) {
			String result = t.apply(input);
			if (result != null) return result;
		}

		return null;
	}
}
