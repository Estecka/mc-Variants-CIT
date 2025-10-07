package fr.estecka.variantscit.format.transforms;

import java.util.Optional;
import org.jetbrains.annotations.Nullable;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.estecka.variantscit.CodecUtil;
import fr.estecka.variantscit.format.IStringTransform;


public record OptionalTransform(IStringTransform inner, @Nullable String fallback)
implements IStringTransform
{
	static public <T extends IStringTransform> MapCodec<IStringTransform> CodecOf(MapCodec<T> inner){
		return RecordCodecBuilder.<IStringTransform>mapCodec(builder->
			builder.group(
				Codec.BOOL.optionalFieldOf("optional", false).forGetter(OptionalTransform::IsOptional),
				Codec.STRING.optionalFieldOf("fallback").forGetter(OptionalTransform::GetFallback),
				CodecUtil.<IStringTransform,T>Anonymize(inner).forGetter(OptionalTransform::Unwrap)
			)
			.apply(builder, OptionalTransform::Wrap)
		);
	}

	static private IStringTransform Wrap(boolean optional, Optional<String> fallback, IStringTransform inner){
		if (optional || fallback.isPresent())
			return new OptionalTransform(inner, fallback.orElse(null));
		else
			return inner;
	}

	static private IStringTransform Unwrap(IStringTransform transform){
		if (transform instanceof OptionalTransform opt)
			return opt.inner;
		else
			return transform;
	}

	static private boolean IsOptional(IStringTransform transform){
		return transform instanceof OptionalTransform;
	}

	static private Optional<String> GetFallback(IStringTransform transform){
		if (transform instanceof OptionalTransform opt)
			return Optional.of(opt.fallback);
		else
			return Optional.empty();
	}

	@Override
	public String apply(final String input) {
		String result = inner.apply(input);
		if (result != null)
			return result;
		else if (fallback != null)
			return fallback;
		else
			return input;
	}
}
