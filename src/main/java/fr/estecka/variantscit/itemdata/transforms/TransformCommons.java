package fr.estecka.variantscit.itemdata.transforms;

import java.util.Optional;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.estecka.variantscit.util.CodecUtil;
import fr.estecka.variantscit.itemdata.containers.IDataContainer;
import fr.estecka.variantscit.itemdata.containers.RawDataContainer;
import fr.estecka.variantscit.itemdata.transforms.impl.LogTransform;
import net.minecraft.nbt.Tag;


public record TransformCommons(
	IDataTransform inner,
	Optional<String> logLabel,
	boolean isOptional,
	Optional<RawDataContainer<Tag>> fallback
)
implements IDataTransform
{
	static public <T extends IDataTransform> MapCodec<IDataTransform> CodecOf(MapCodec<T> inner){
		return RecordCodecBuilder.<IDataTransform>mapCodec(builder->
			builder.group(
				Codec.BOOL.optionalFieldOf("optional", false).forGetter(CodecUtil.NoGetter("isOptional")),
				RawDataContainer.LITTERAL_CODEC.optionalFieldOf("fallback").forGetter(CodecUtil.NoGetter("fallback")),
				Codec.STRING.optionalFieldOf("logLabel").forGetter(CodecUtil.NoGetter("logLabel")),
				CodecUtil.<IDataTransform,T>AnonymizeMap(inner).forGetter(CodecUtil.NoGetter("inner"))
			)
			.apply(builder, TransformCommons::Wrap)
		);
	}

	static private IDataTransform Wrap(boolean isOptional, Optional<RawDataContainer<Tag>> fallback, Optional<String> logLabel, IDataTransform inner){
		isOptional |= fallback.isPresent();
		return new TransformCommons(inner, logLabel, isOptional, fallback).UnwrapIfNoop();
	}

	public IDataTransform UnwrapIfNoop(){
		if (this.logLabel.isEmpty() && !this.isOptional)
			return this.inner;
		else
			return this;
	}

	@Override
	public IDataContainer LooseTypedTransform(final IDataContainer input) {
		IDataContainer result = inner.LooseTypedTransform(input);
		if (this.logLabel.isPresent())
			LogTransform.Log(logLabel, result);

		if (isOptional && result==null)
			return fallback.isPresent() ? fallback.get() : input;
		else
			return result;
	}
}
