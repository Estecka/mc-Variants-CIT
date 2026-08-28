package fr.estecka.variantscit.itemdata.transforms.impl;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.estecka.variantscit.util.CodecUtil;
import fr.estecka.variantscit.itemdata.containers.IDataContainer;
import fr.estecka.variantscit.itemdata.containers.RawDataContainer;
import fr.estecka.variantscit.itemdata.transforms.IDataTransform;
import fr.estecka.variantscit.itemdata.transforms.SuccessiveTransform;
import net.minecraft.nbt.CollectionTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

public record ForeachTransform(
	IDataIterator iterator,
	IDataTransform foreachDo,
	IReturnType returnType
)
implements IDataTransform
{
	@FunctionalInterface
	static private interface IReturnType
	extends BiFunction<ForeachTransform, IDataContainer, IDataContainer>
	{}

	static private final Codec<IReturnType> RETURN_CODEC = CodecUtil.Enum(Codec.STRING, Map.of(
		"first_match", ForeachTransform::FirstMatch,
		"matches_any", ForeachTransform::MatchesAny,
		"matches_all", ForeachTransform::MatchesAll
	));


	@FunctionalInterface
	static private interface IDataIterator
	extends Function<IDataContainer, Stream<IDataContainer>>
	{}

	static private final Codec<IDataIterator> ENUM_ITERATOR_CODEC = CodecUtil.Enum(Codec.STRING, Map.of(
		"value",     ForeachTransform::ValuesIterator,
		"key",       ForeachTransform::KeysIterator,
		"key_value", ForeachTransform::EntriesIterator
	));

	static private final Codec<IDataIterator> TRANSFORM_ITERATOR_CODEC = SuccessiveTransform.CODEC.listOf()
		.xmap(list -> input -> TransformIterator(input, list), CodecUtil.NoGetter("Foreach TransformIterator"))
		;

	static private final Codec<IDataIterator> ITERATOR_CODEC = CodecUtil.WithAlternative(ENUM_ITERATOR_CODEC, TRANSFORM_ITERATOR_CODEC);

	static public final MapCodec<ForeachTransform> MAPCODEC = RecordCodecBuilder.mapCodec(builder->
		builder.group(
			ITERATOR_CODEC.fieldOf("foreach").forGetter(ForeachTransform::iterator),
			SuccessiveTransform.CODEC.fieldOf("do").forGetter(ForeachTransform::foreachDo),
			RETURN_CODEC.fieldOf("return").forGetter(ForeachTransform::returnType)
		)
		.apply(builder, ForeachTransform::new)
	);

	@Override
	public IDataContainer LooseTypedTransform(IDataContainer input) {
		return returnType.apply(this, input);
	}


	static private CompoundTag CompoundEntry(String key, CompoundTag compound){
		CompoundTag r = new CompoundTag();
		r.putString("key", key);
		r.put("value", compound.get(key));
		return r;
	}

	static private CompoundTag ArrayEntry(int index, CollectionTag list){
		CompoundTag r = new CompoundTag();
		r.putInt("key", index);
		r.put("value", list.get(index));
		return r;
	}

	static private Stream<Integer> StreamIndices(Integer length){
		return Stream.iterate(0, i->i<length, i->++i);
	}


/******************************************************************************/
/* # Iterators                                                                */
/******************************************************************************/

	static private Stream<IDataContainer> TransformIterator(IDataContainer input, List<IDataTransform> foreach){
		return foreach.stream()
			.map(transform -> transform.LooseTypedTransform(input))
			;
	}

	static private Stream<IDataContainer> ValuesIterator(IDataContainer input){
		Tag nbt = input.asNbt();

		if (nbt instanceof CollectionTag list)
			return list.stream().map(RawDataContainer::OfNullable);
		else if (nbt instanceof CompoundTag compound)
			return compound.keySet().stream().map(compound::get).map(RawDataContainer::OfNullable);
		else
			return null;
	}

	static private Stream<IDataContainer> KeysIterator(IDataContainer input){
		Tag nbt = input.asNbt();

		if (nbt instanceof CollectionTag list)
			return StreamIndices(list.size()).map(RawDataContainer::OfNullable);
		else if (nbt instanceof CompoundTag compound)
			return compound.keySet().stream().map(RawDataContainer::OfNullable);
		else
			return null;
	}

	static private Stream<IDataContainer> EntriesIterator(IDataContainer input){
		Tag nbt = input.asNbt();

		if (nbt instanceof CollectionTag list)
			return StreamIndices(list.size()).map(i->ArrayEntry(i, list)).map(RawDataContainer::OfNullable);
		else if (nbt instanceof CompoundTag compound)
			return compound.keySet().stream().map(key->CompoundEntry(key, compound)).map(RawDataContainer::OfNullable);
		else
			return null;
	}


/******************************************************************************/
/* # Return                                                                   */
/******************************************************************************/

	private IDataContainer FirstMatch(IDataContainer input){
		return this.iterator.apply(input)
			.map(foreachDo::LooseTypedTransform)
			.filter(data -> data != null)
			.findFirst()
			.orElse(null)
			;
	}

	private IDataContainer MatchesAny(IDataContainer input){
		return this.iterator.apply(input)
			.map(foreachDo::LooseTypedTransform)
			.filter(data -> data != null)
			.findFirst()
			.map(data -> input)
			.orElse(null)
			;
	}

	private IDataContainer MatchesAll(IDataContainer input){
		return this.iterator.apply(input)
			.map(foreachDo::LooseTypedTransform)
			.map(data -> data != null) // Cast to boolean. findFirst cannot be used to find null.
			.filter(matches -> !matches) // Find first failure
			.findFirst()
			.isPresent() ? null : input
			;
	}

}
