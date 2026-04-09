package fr.estecka.variantscit.itemdata.transforms.impl;

import java.util.List;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import fr.estecka.variantscit.itemdata.containers.IDataContainer;
import fr.estecka.variantscit.itemdata.transforms.IDataTransform;
import fr.estecka.variantscit.itemdata.transforms.SuccessiveTransform;

// TODO: Deprecate and replace with an implementation of ForeachTransform
public record MatchesTransform(
	List<IDataTransform> subTransforms,
	boolean matchesAny
)
implements IDataTransform
{
	static public final boolean MATCHANY_TYPE = true;
	static public final boolean MATCHALL_TYPE = false;

	static private final Codec<MatchesTransform> MATCHANY_CODEC = TypedCodec(MATCHANY_TYPE);
	static private final Codec<MatchesTransform> MATCHALL_CODEC = TypedCodec(MATCHALL_TYPE);

	static public final MapCodec<MatchesTransform> MATCHANY_MAPCODEC = MATCHANY_CODEC.fieldOf("matches_any");
	static public final MapCodec<MatchesTransform> MATCHALL_MAPCODEC = MATCHALL_CODEC.fieldOf("matches_all");

	static private Codec<MatchesTransform> TypedCodec(boolean groupType){
		return SuccessiveTransform.CODEC.listOf()
			.xmap(list->new MatchesTransform(list, groupType), MatchesTransform::subTransforms)
			;
	}
	
	@Override
	public IDataContainer LooseTypedTransform(IDataContainer input) {
		for (IDataTransform t : subTransforms){
			var r = t.LooseTypedTransform(input);
			if (matchesAny && r != null)
				return input;
			else if (!matchesAny && r == null)
				return null;
		}

		return matchesAny ? null : input;
	}
}
