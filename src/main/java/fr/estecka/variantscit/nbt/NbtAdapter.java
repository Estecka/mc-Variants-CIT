package fr.estecka.variantscit.nbt;

import java.text.Normalizer;
import java.util.List;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.estecka.variantscit.CodecUtil;
import net.minecraft.nbt.AbstractNbtNumber;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtString;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;

public class NbtAdapter
{
	static public final MapCodec<NbtAdapter> MAP_CODEC = RecordCodecBuilder.mapCodec(builder->builder
		.group(
			NbtPath.CODEC.fieldOf("nbtPath").forGetter(adp -> adp.nbtPath),
			EInput.CODEC.fieldOf("input").orElse(EInput.LITERAL).forGetter(adp -> adp.type),
			CodecUtil.OneOrMany(EFilter.CODEC).fieldOf("filter").orElse(List.of()).forGetter(adp -> List.of(adp.filters))
		)
		.apply(builder, NbtAdapter::new)
	);

	static public final Codec<NbtAdapter> CODEC = Codec.withAlternative(
		Codec.of(MAP_CODEC.encoder(), MAP_CODEC.decoder()),
		NbtPath.CODEC.xmap(path->new NbtAdapter(path, EInput.LITERAL, List.of()), adp->adp.nbtPath)
	);

	private final String[] nbtPath;
	private final EInput type;
	private final EFilter[] filters;


	protected NbtAdapter(String[] nbtPath, EInput type, List<EFilter> filters){
		this.nbtPath = nbtPath;
		this.type = type;
		this.filters = filters.toArray(EFilter[]::new);
	}

	public final @Nullable String ResolveData(NbtElement nbt){
		nbt = NbtPath.Resolve(nbt, nbtPath);
		if (nbt == null)
			return null;

		String data = this.type.Accept(nbt);
		if (data == null)
			return null;

		for (EFilter filter : this.filters)
			data = filter.Transform(data);
		return data;
	}

	public enum EInput
	implements StringIdentifiable
	{
		LITERAL("literal", (nbt)->{
			if (nbt instanceof NbtString)
				return nbt.asString();
			else if (nbt instanceof AbstractNbtNumber number)
				return number.numberValue().toString();
			else
				return null;
		}),

		NUMBER("number", (nbt)-> (nbt instanceof AbstractNbtNumber number) ? number.numberValue().toString() : null),
		STRING("string", (nbt)-> (nbt instanceof NbtString) ? nbt.asString() : null),
		IDENTIFIER("identifier", (nbt)-> (nbt instanceof NbtString && Identifier.tryParse(nbt.asString()) != null) ? nbt.asString() : null),
		;

		static public final Codec<EInput> CODEC = StringIdentifiable.createCodec(EInput::values);

		private final String name;
		private final Function<NbtElement,String> lambda;

		private EInput(String name, Function<NbtElement,String> lambda){
			this.name = name;
			this.lambda = lambda;
		}

		public @Nullable String Accept(NbtElement nbt){
			return this.lambda.apply(nbt);
		}
	
		@Override public String asString(){
			return this.name;
		}
	
	}

	public enum EFilter
	implements StringIdentifiable
	{
		NOOP("noop", Function.identity()),
		CASE_INSENSITIVE("case_insensitive", String::toLowerCase),

		SANITIZED_PATH("sanitized_path", s->Sanitize(s, "[^a-zA-Z0-9_.-/]")),
		SANITIZED_NAMESPACE("sanitized_namespace", s->Sanitize(s, "[^a-zA-Z0-9_.-]")),
		SANITIZED_ID("sanitized", s->Sanitize(s, "[^a-zA-Z0-9_.-/:]")),

		DISCARD_NAMESPACE("discard_namespace", s->{
			int split = s.lastIndexOf(':');
			return (split < 0) ? s : s.substring(split);
		}),
		DISCARD_PATH("discard_path", s->{
			int split = s.indexOf(':');
			return (split < 0) ? s : s.substring(0, split);
		}),
		;
	
		static public final Codec<EFilter> CODEC = StringIdentifiable.createCodec(EFilter::values);
	
		private final String name;
		private final Function<String,String> lambda;
	
		private EFilter(String name, Function<String,String> lambda){
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

		public @NotNull String Transform(String nbt){
			return this.lambda.apply(nbt);
		}
	
		@Override public String asString(){
			return this.name;
		}
	
	}
	
}
