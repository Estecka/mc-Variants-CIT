package fr.estecka.variantscit.itemdata.transforms.impl;

import java.util.Map;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import fr.estecka.variantscit.itemdata.containers.IDataContainer;
import fr.estecka.variantscit.itemdata.containers.RawDataContainer;
import fr.estecka.variantscit.itemdata.transforms.IDataTransform;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;


public record RemapTransform<T>(
	Function<IDataContainer, T> dataGetter,
	Map<T, RawDataContainer<Tag>> map
)
implements IDataTransform.NullSafe
{
	static public final Codec<Double> DOUBLE_STRING = Codec.STRING.xmap(Double::parseDouble, Number::toString);

	static public final MapCodec<RemapTransform<String>> MAPCODEC_STRING = CreateCodec(Codec.STRING, IDataContainer::asString);
	static public final MapCodec<RemapTransform<Double>> MAPCODEC_NUMBER = CreateCodec(DOUBLE_STRING, IDataContainer::asDouble);
	static public final MapCodec<RemapTransform<Identifier>> MAPCODEC_ID = CreateCodec(Identifier.CODEC, IDataContainer::asIdentifier);

	static public <T> MapCodec<RemapTransform<T>> CreateCodec(Codec<T> keyCodec, Function<IDataContainer, T> dataGetter){
		return Codec.unboundedMap(keyCodec, RawDataContainer.LITTERAL_CODEC)
			.fieldOf("map")
			.xmap(map -> new RemapTransform<T>(dataGetter, map), RemapTransform::map)
			;
	}

	@Override
	public @Nullable IDataContainer NullSafeTransform(@NotNull IDataContainer input) {
		T data = dataGetter.apply(input);
		if (data != null)
			return map.get(data);
		else
			return null;
	}
}
