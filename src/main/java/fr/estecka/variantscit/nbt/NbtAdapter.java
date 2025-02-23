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
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;

public class NbtAdapter
{
	static public final MapCodec<NbtAdapter> MAPCODEC = RecordCodecBuilder.mapCodec(builder->builder
		.group(
			NbtPath.CODEC.fieldOf("nbtPath").forGetter(adp -> adp.nbtPath),
			EInput.CODEC.fieldOf("expect").orElse(EInput.PRIMITIVE).forGetter(adp -> adp.type),
			CodecUtil.OneOrMany(ETransform.CODEC).fieldOf("transform").orElse(List.of()).forGetter(adp -> List.of(adp.transforms))
		)
		.apply(builder, NbtAdapter::new)
	);

	static public final Codec<NbtAdapter> CODEC = Codec.withAlternative(
		MAPCODEC.codec(),
		NbtPath.CODEC.xmap(path->new NbtAdapter(path, EInput.PRIMITIVE, List.of()), adp->adp.nbtPath)
	);

	@Deprecated
	static public final MapCodec<NbtAdapter> LEGACY_MAPCODEC = RecordCodecBuilder.mapCodec(builder->builder
		.group(
			CodecUtil.MapWithAlternative(
				NbtPath.LEGACY_CODEC.fieldOf("nbtPath"),
				NbtPath.NBTKEY_CODEC.fieldOf("nbtKey")
			).forGetter(s->s.nbtPath),
			Codec.BOOL.fieldOf("caseSensitive").forGetter(s->true)
		)
		.apply(builder, (path, lowercase) -> new NbtAdapter(path, EInput.PRIMITIVE, lowercase?List.of(ETransform.LOWERCASE):List.of()))
	);

	private final NbtPath nbtPath;
	private final EInput type;
	private final ETransform[] transforms;


	protected NbtAdapter(NbtPath nbtPath, EInput type, List<ETransform> filters){
		this.nbtPath = nbtPath;
		this.type = type;
		this.transforms = filters.toArray(ETransform[]::new);
	}

	public final @Nullable String ResolveData(NbtElement nbt){
		nbt = this.nbtPath.Resolve(nbt);
		if (nbt == null)
			return null;

		String data = this.type.Accept(nbt);
		if (data == null)
			return null;

		for (ETransform filter : this.transforms)
			data = filter.Transform(data);
		return data;
	}

	public enum EInput
	implements StringIdentifiable
	{
		PRIMITIVE("primitive", (nbt)->{
			if (nbt instanceof NbtString)
				return nbt.asString();
			else if (nbt instanceof AbstractNbtNumber number)
				return number.numberValue().toString();
			else
				return null;
		}),

		NUMBER("number", (nbt)-> (nbt instanceof AbstractNbtNumber number) ? number.numberValue().toString() : null),
		STRING("string", (nbt)-> (nbt instanceof NbtString) ? nbt.asString() : null),
		IDENTIFIER("identifier", (nbt)-> (nbt instanceof NbtString) ? Identifier.tryParse(nbt.asString()).toString() : null),
		RICH_TEXT("rich_text", (nbt)->{
			var result = TextCodecs.STRINGIFIED_CODEC.parse(NbtOps.INSTANCE, nbt);
			if (result.isSuccess())
				return result.getOrThrow().getString();
			else
				return null;
		}),
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

		public @NotNull String Transform(String nbt){
			return this.lambda.apply(nbt);
		}
	
		@Override public String asString(){
			return this.name;
		}
	
	}
	
}
