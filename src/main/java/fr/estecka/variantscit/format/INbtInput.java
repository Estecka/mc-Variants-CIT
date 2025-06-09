package fr.estecka.variantscit.format;

import java.util.List;
import java.util.function.Function;
import com.mojang.serialization.Codec;
import fr.estecka.variantscit.CodecUtil;
import fr.estecka.variantscit.VCitRegistries;
import net.minecraft.nbt.AbstractNbtNumber;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.Identifier;

public interface INbtInput
extends Function<NbtElement,String>
{
	static public final INbtInput AUTO = Grouped(INbtInput::String, INbtInput::Number, INbtInput::RichText, INbtInput::RichTextArray);

	static public final Codec<INbtInput> CODEC = VCitRegistries.NBT_INPUTS.codec;
	static public final Codec<INbtInput[]> ARRAY_CODEC = CodecUtil.OneOrMany(CODEC).xmap(list->list.toArray(INbtInput[]::new), array->List.<INbtInput>of(array));

	static public INbtInput Grouped(INbtInput... group){
		if (group.length == 1)
			return group[0];

		return (NbtElement nbt) -> {
			for (int i=0; i<group.length; ++i){
				String result = group[i].apply(nbt);
				if (result != null)
					return result;
			}
			return null;
		};
	}

	static public String String (NbtElement nbt) { return nbt instanceof NbtString string ? string.asString() : null; }
	static public String Number (NbtElement nbt) { return nbt instanceof AbstractNbtNumber number ? number.numberValue().toString() : null; }
	static public String Identifier (NbtElement nbt) { return nbt instanceof NbtString string ? Identifier.tryParse(nbt.asString()).toString() : null; }

	static public String RichText(NbtElement nbt){
		var text = TextCodecs.STRINGIFIED_CODEC.parse(NbtOps.INSTANCE, nbt);
		if (text.isSuccess())
			return text.getOrThrow().getString();

		return null;
	}

	static public String RichTextArray(NbtElement nbt){
		var result = TextCodecs.STRINGIFIED_CODEC.sizeLimitedListOf(256).parse(NbtOps.INSTANCE, nbt);
		if (!result.isSuccess())
			return null;

		List<Text> lines = result.getOrThrow();
		StringBuilder builder = new StringBuilder();
		for (var l : lines) {
			builder.append(l.getString());
			builder.append('\n');
		}

		return builder.toString();
	}
}
