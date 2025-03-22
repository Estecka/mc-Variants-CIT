package fr.estecka.variantscit.format;

import java.text.Normalizer;
import java.util.List;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;
import com.mojang.serialization.Codec;
import fr.estecka.variantscit.CodecUtil;
import net.minecraft.util.StringIdentifiable;

public enum ETransform
implements StringIdentifiable
{
	NOOP("noop", Function.identity()),
	LOWERCASE("lowercase", String::toLowerCase),

	SANITIZE("sanitize", s->Sanitize(s, "[^a-zA-Z0-9_.-/:]")),
	SANITIZE_PATH("sanitize_path", s->Sanitize(s, "[^a-zA-Z0-9_.-/]")),
	SANITIZE_NAMESPACE("sanitize_namespace", s->Sanitize(s, "[^a-zA-Z0-9_.-]")),

	DISCARD_NAMESPACE("discard_namespace", s->{
		int split = s.lastIndexOf(':');
		return (split < 0) ? s : s.substring(split);
	}),
	DISCARD_PATH("discard_path", s->{
		int split = s.lastIndexOf(':');
		return (split < 0) ? "" : s.substring(0, split);
	}),
	;

	static public final Codec<ETransform> CODEC = StringIdentifiable.createCodec(ETransform::values);
	static public final Codec<ETransform[]> ARRAY_CODEC = CodecUtil.OneOrMany(CODEC).xmap(list->list.toArray(ETransform[]::new), array->List.<ETransform>of(array));

	private final String name;
	private final Function<String,String> lambda;

	private ETransform(String name, Function<String,String> lambda){
		this.name = name;
		this.lambda = lambda;
	}

	static private String Sanitize(String input, String charset){
		return Normalizer.normalize(input, Normalizer.Form.NFD)
			.replace(' ', '_')
			.toLowerCase()
			.replaceAll(charset, "")
			;
	}

	static public String Transform(ETransform[] transforms, String input){
		for (ETransform t : transforms)
			input = t.Transform(input);
		return input;
	}

	public @NotNull String Transform(String nbt){
		return this.lambda.apply(nbt);
	}

	@Override public String asString(){
		return this.name;
	}

}
