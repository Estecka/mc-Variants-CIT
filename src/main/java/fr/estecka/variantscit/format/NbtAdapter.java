package fr.estecka.variantscit.format;

import java.util.function.Function;
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
			EInput.CODEC.fieldOf("expect").orElse(EInput.PRIMITIVE).forGetter(adp -> adp.type)
		)
		.apply(builder, NbtAdapter::new)
	);

	static public final Codec<NbtAdapter> CODEC = Codec.withAlternative(
		MAPCODEC.codec(),
		NbtPath.CODEC.xmap(path->new NbtAdapter(path, EInput.PRIMITIVE), adp->adp.nbtPath)
	);

	@Deprecated
	static public final MapCodec<NbtAdapter> LEGACY_MAPCODEC = CodecUtil.MapWithAlternatives(
		NbtPath.CODEC.fieldOf("nbtPath"),
		NbtPath.DOT_SEPARATED_CODEC.fieldOf("nbtPath"),
		NbtPath.NBTKEY_CODEC.fieldOf("nbtKey")
	).xmap((path)->new NbtAdapter(path,EInput.PRIMITIVE), (adp)->adp.nbtPath);

	private final NbtPath nbtPath;
	private final EInput type;


	protected NbtAdapter(NbtPath nbtPath, EInput type){
		this.nbtPath = nbtPath;
		this.type = type;
	}

	public final @Nullable String ResolveData(NbtElement nbt){
		nbt = this.nbtPath.Resolve(nbt);
		if (nbt == null)
			return null;

		String data = this.type.Accept(nbt);
		if (data == null)
			return null;

		return data;
	}

	public enum EInput
	implements StringIdentifiable
	{
		PRIMITIVE("primitive", (nbt)->{
			if (nbt instanceof NbtString string)
				return string.value();
			else if (nbt instanceof AbstractNbtNumber number)
				return number.numberValue().toString();
			else
				return null;
		}),

		NUMBER("number", (nbt)-> (nbt instanceof AbstractNbtNumber number) ? number.numberValue().toString() : null),
		STRING("string", (nbt)-> (nbt instanceof NbtString string) ? string.value() : null),
		IDENTIFIER("identifier", (nbt)-> (nbt instanceof NbtString string) ? Identifier.tryParse(string.value()).toString() : null),
		RICH_TEXT("rich_text", (nbt)->{
			var result = TextCodecs.CODEC.parse(NbtOps.INSTANCE, nbt);
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

}
