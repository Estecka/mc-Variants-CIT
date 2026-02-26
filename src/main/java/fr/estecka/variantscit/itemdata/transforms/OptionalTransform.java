package fr.estecka.variantscit.itemdata.transforms;

import java.util.Optional;
import org.jetbrains.annotations.Nullable;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.estecka.variantscit.CodecUtil;
import fr.estecka.variantscit.itemdata.containers.IDataContainer;
import fr.estecka.variantscit.itemdata.containers.RawDataContainer;
import net.minecraft.nbt.Tag;


public record OptionalTransform(IDataTransform inner, @Nullable RawDataContainer<Tag> fallback)
implements IDataTransform
{
	static public <T extends IDataTransform> MapCodec<IDataTransform> CodecOf(MapCodec<T> inner){
		return RecordCodecBuilder.<IDataTransform>mapCodec(builder->
			builder.group(
				Codec.BOOL.optionalFieldOf("optional", false).forGetter(OptionalTransform::IsOptional),
				RawDataContainer.LITTERAL_CODEC.optionalFieldOf("fallback").forGetter(OptionalTransform::GetFallback),
				CodecUtil.<IDataTransform,T>Anonymize(inner).forGetter(OptionalTransform::Unwrap)
			)
			.apply(builder, OptionalTransform::Wrap)
		);
	}

	static private IDataTransform Wrap(boolean optional, Optional<RawDataContainer<Tag>> fallback, IDataTransform inner){
		if (optional || fallback.isPresent())
			return new OptionalTransform(inner, fallback.orElse(null));
		else
			return inner;
	}

	static private IDataTransform Unwrap(IDataTransform transform){
		if (transform instanceof OptionalTransform opt)
			return opt.inner;
		else
			return transform;
	}

	static private boolean IsOptional(IDataTransform transform){
		return transform instanceof OptionalTransform;
	}

	static private Optional<RawDataContainer<Tag>> GetFallback(IDataTransform transform){
		if (transform instanceof OptionalTransform opt)
			return Optional.of(opt.fallback);
		else
			return Optional.empty();
	}

	@Override
	public IDataContainer LooseTypedTransform(IDataContainer input) {
		IDataContainer result = inner.LooseTypedTransform(input);
		if (result != null)
			return result;
		else if (fallback != null)
			return fallback;
		else
			return input;
	}
}
