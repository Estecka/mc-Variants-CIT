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
	static public final INbtInput AUTO = Grouped(INbtInput::RichText, INbtInput::String, INbtInput::Number, INbtInput::RichTextArray);
	static public final INbtInput PRIMITIVE = Grouped(INbtInput::String, INbtInput::Number);

	static public final Codec<INbtInput> CODEC = VCitRegistries.NBT_INPUTS.codec;
	static public final Codec<INbtInput[]> ARRAY_CODEC = CodecUtil.OneOrMany(CODEC).xmap(list->list.toArray(INbtInput[]::new), array->List.<INbtInput>of(array));
	static public final Codec<INbtInput> GROUP_CODEC = ARRAY_CODEC.xmap(INbtInput::Grouped, type -> type instanceof Group group ? group.content() : new INbtInput[]{ type } );

	public record Group(INbtInput... content)
	implements INbtInput
	{
		public String apply(NbtElement nbt){
			for (int i=0; i<content.length; ++i){
				String result = content[i].apply(nbt);
				if (result != null)
					return result;
			}
			return null;
		}

	}

	static public INbtInput Grouped(INbtInput... group){
		if (group.length == 1)
			return group[0];
		else
			return new Group(group);
	}

	static public String String (NbtElement nbt) { return nbt instanceof NbtString string ? string.value() : null; }
	static public String Number (NbtElement nbt) { return nbt instanceof AbstractNbtNumber number ? number.numberValue().toString() : null; }
	static public String Identifier (NbtElement nbt) {
		Identifier id;
		if (nbt instanceof NbtString string && null != (id=Identifier.tryParse(string.value())))
			return id.toString();
		else
			return null;
	}

	static public String RichText(NbtElement nbt){
		var text = TextCodecs.CODEC.parse(NbtOps.INSTANCE, nbt);
		if (text.isSuccess())
			return text.getOrThrow().getString();

		return null;
	}

	static public String RichTextArray(NbtElement nbt){
		var result = TextCodecs.CODEC.sizeLimitedListOf(256).parse(NbtOps.INSTANCE, nbt);
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
