package fr.estecka.variantscit.itemdata.functions.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import fr.estecka.variantscit.itemdata.functions.IStringTransform;

public record FilterlistTransform(Set<String> filter, boolean isWhiteList)
implements IStringTransform
{
	static public final MapCodec<FilterlistTransform> MAPCODEC_WHITELIST = CreateMapCodec(true);
	static public final MapCodec<FilterlistTransform> MAPCODEC_BLACKLIST = CreateMapCodec(false);

	static public final MapCodec<FilterlistTransform> CreateMapCodec(boolean isWhiteList){
		String listName = isWhiteList ? "whitelist" : "blacklist";
		return Codec.STRING
			.listOf()
			.fieldOf(listName)
			.<Set<String>>xmap(HashSet::new, List::copyOf)
			.xmap(set->new FilterlistTransform(set, isWhiteList), FilterlistTransform::filter)
			;
	}

	@Override
	public String apply(String input) {
		if (isWhiteList == filter.contains(input))
			return input;
		else
			return null;
	}
}
