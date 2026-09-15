package fr.estecka.variantscit.itemdata.transforms.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import fr.estecka.variantscit.itemdata.containers.IDataContainer;
import fr.estecka.variantscit.itemdata.transforms.IDataTransform;
import net.minecraft.resources.Identifier;


public record FilterlistTransform<T>(
	Function<IDataContainer, T> dataGetter,
	Set<T> filter,
	boolean isWhiteList
)
implements IDataTransform.NullSafe
{
	static public final MapCodec<FilterlistTransform<String>> MAPCODEC_WHITELIST_STRING = CreateMapCodec(Codec.STRING, IDataContainer::asString, true);
	static public final MapCodec<FilterlistTransform<String>> MAPCODEC_BLACKLIST_STRING = CreateMapCodec(Codec.STRING, IDataContainer::asString, false);
	static public final MapCodec<FilterlistTransform<Double>> MAPCODEC_WHITELIST_NUMBER = CreateMapCodec(Codec.DOUBLE, IDataContainer::asDouble, true);
	static public final MapCodec<FilterlistTransform<Double>> MAPCODEC_BLACKLIST_NUMBER = CreateMapCodec(Codec.DOUBLE, IDataContainer::asDouble, false);
	static public final MapCodec<FilterlistTransform<Identifier>> MAPCODEC_WHITELIST_ID = CreateMapCodec(Identifier.CODEC, IDataContainer::asIdentifier, true);
	static public final MapCodec<FilterlistTransform<Identifier>> MAPCODEC_BLACKLIST_ID = CreateMapCodec(Identifier.CODEC, IDataContainer::asIdentifier, false);

	static public final <T> MapCodec<FilterlistTransform<T>> CreateMapCodec(
		Codec<T> entryCodec,
		Function<IDataContainer, T> dataGetter,
		boolean isWhiteList
	){
		String listName = isWhiteList ? "whitelist" : "blacklist";
		return entryCodec
			.listOf()
			.fieldOf(listName)
			.<Set<T>>xmap(HashSet::new, List::copyOf)
			.xmap(set->new FilterlistTransform<T>(dataGetter, set, isWhiteList), FilterlistTransform::filter)
			;
	}

	@Override
	public IDataContainer NullSafeTransform(IDataContainer input) {
		T data = dataGetter.apply(input);
		if (isWhiteList == filter.contains(data))
			return input;
		else
			return null;
	}
}
