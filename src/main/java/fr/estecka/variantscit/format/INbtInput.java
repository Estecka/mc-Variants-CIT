package fr.estecka.variantscit.format;

import java.util.List;
import java.util.function.Function;
import com.mojang.serialization.Codec;
import fr.estecka.variantscit.CodecUtil;
import fr.estecka.variantscit.VCitRegistries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceLocation;

public interface INbtInput
extends Function<Tag,String>
{
	static public final INbtInput AUTO = Grouped(INbtInput::RichText, INbtInput::String, INbtInput::Number, INbtInput::RichTextArray);
	static public final INbtInput PRIMITIVE = Grouped(INbtInput::String, INbtInput::Number);

	static public final Codec<INbtInput> CODEC = VCitRegistries.NBT_INPUTS.codec;
	static public final Codec<INbtInput[]> ARRAY_CODEC = CodecUtil.OneOrMany(CODEC).xmap(list->list.toArray(INbtInput[]::new), array->List.<INbtInput>of(array));
	static public final Codec<INbtInput> GROUP_CODEC = ARRAY_CODEC.xmap(INbtInput::Grouped, type -> type instanceof Group group ? group.content() : new INbtInput[]{ type } );

	public record Group(INbtInput... content)
	implements INbtInput
	{
		public String apply(Tag nbt){
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

	static public String String (Tag nbt) { return nbt instanceof StringTag string ? string.getAsString() : null; }
	static public String Number (Tag nbt) { return nbt instanceof NumericTag number ? number.getAsNumber().toString() : null; }
	static public String Identifier (Tag nbt) {
		ResourceLocation id;
		if (nbt instanceof StringTag && null != (id=ResourceLocation.tryParse(nbt.getAsString())))
			return id.toString();
		else
			return null;
	}

	static public String RichText(Tag nbt){
		var text = ComponentSerialization.FLAT_CODEC.parse(NbtOps.INSTANCE, nbt);
		if (text.isSuccess())
			return text.getOrThrow().getString();

		return null;
	}

	static public String RichTextArray(Tag nbt){
		var result = ComponentSerialization.FLAT_CODEC.sizeLimitedListOf(256).parse(NbtOps.INSTANCE, nbt);
		if (!result.isSuccess())
			return null;

		List<Component> lines = result.getOrThrow();
		StringBuilder builder = new StringBuilder();
		for (var l : lines) {
			builder.append(l.getString());
			builder.append('\n');
		}

		return builder.toString();
	}
}
