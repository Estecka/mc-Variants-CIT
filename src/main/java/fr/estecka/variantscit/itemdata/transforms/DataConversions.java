package fr.estecka.variantscit.itemdata.transforms;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import fr.estecka.variantscit.CodecUtil;
import fr.estecka.variantscit.itemdata.containers.IDataContainer;
import fr.estecka.variantscit.itemdata.containers.RawDataContainer;
import fr.estecka.variantscit.itemdata.transforms.impl.AlternativeTransform;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.component.ItemLore;

public final class DataConversions
{
	static public final Codec<IDataTransform> EXPECT_UNIT_CODEC = CodecUtil.Enum(Codec.STRING, Map.of(
		"string",          DataConversions::StrictString,
		"identifier",      DataConversions::StricIdentifier,
		"number",          DataConversions::StrictNumber,
		"rich_text",       DataConversions::StrictRichText,
		"rich_text_array", DataConversions::StrictRichTextArray
	));

	static public final Codec<IDataTransform> EXPECT_GROUP_CODEC = CodecUtil.OneOrMany(EXPECT_UNIT_CODEC)
		.xmap(AlternativeTransform::Wrap, AlternativeTransform::Unwrap)
		;

	static public final MapCodec<IDataTransform> GET_IDENTIFIER_MAPCODEC = CodecUtil.IDENTIFIER_NAMESPACE
		.optionalFieldOf("defaultNamespace", "minecraft")
		.xmap(
			namespace -> (data)->StricIdentifier(data, namespace),
			CodecUtil.NoGetter("get_identifier")
		)
		;


/******************************************************************************/
/* # Strict Casts                                                             */
/******************************************************************************/

	static public IDataContainer StricIdentifier(IDataContainer input) {
		return StricIdentifier(input, ResourceLocation.DEFAULT_NAMESPACE);
	}

	static public IDataContainer StricIdentifier(IDataContainer input, String defaultNamespace) {
		if (input.value() instanceof ResourceLocation)
			return input;
		if (input.value() instanceof String string)
			return RawDataContainer.OfNullable(CodecUtil.NamespacedIdentifier(defaultNamespace, string).mapOrElse(o->o, o->null));
		if (input.asNbt() instanceof StringTag nbt)
			return RawDataContainer.OfNullable(CodecUtil.NamespacedIdentifier(defaultNamespace, nbt.getAsString()).mapOrElse(o->o, o->null));
		return null;
	}

	static public IDataContainer StrictString(IDataContainer input) {
		if (input.value() instanceof String)
			return input;
		if (input.value() instanceof ResourceLocation id)
			return RawDataContainer.OfNullable(id.toString());
		if (input.asNbt() instanceof StringTag nbt)
			return RawDataContainer.OfNullable(nbt.getAsString());
		return null;
	}

	static public IDataContainer StrictNumber(IDataContainer input) {
		if (input.value() instanceof Number)
			return input;
		if (input.asNbt() instanceof NumericTag nbt)
			return RawDataContainer.OfNullable(nbt.getAsNumber());
		return null;
	}

	static public IDataContainer StrictRichText(IDataContainer input) {
		if (input.value() instanceof Component)
			return input;
		
		var nbt = input.asNbt();
		if (nbt != null)
			return RawDataContainer.OfNullable(NbtToText(nbt));
		else
			return null;
	}

	static public IDataContainer StrictRichTextArray(IDataContainer input) {
		if (input.value() instanceof ItemLore)
			return input;
		
		var nbt = input.asNbt();
		if (nbt != null)
			return RawDataContainer.OfNullable(NbtToTextArray(nbt));
		else
			return null;
	}

	static public IDataContainer StrictNbt(IDataContainer input) {
		if (input.value() instanceof Tag)
			return input;
		
		return RawDataContainer.OfNullable(input.asNbt());
	}

	static public IDataContainer StrictSnbt(IDataContainer input) {
		input = StrictNbt(input);
		if (input == null)
			return null;

		Tag nbt = (Tag)input.value();
		String snbt = nbt.getAsString();
		return RawDataContainer.OfNullable(snbt);
	}


/******************************************************************************/
/* # Aggressive Conversions                                                   */
/******************************************************************************/

	static public String AggressiveString(IDataContainer data){
		String result = null;

		result = SoftCastToString(data.value());
		if (result != null)
			return result;

		Tag nbt = data.asNbt();
		if (nbt == null)
			return null;

		result = SoftCastToString(NbtToText(nbt));
		if (result != null)
			return result;

		var lines = NbtToTextArray(nbt);
		if (lines != null)
			return TextArrayToString(lines);

		return null;
	}

	static public Number AggressiveNumber(IDataContainer data){
		Number result = null;

		result = SoftCastToNumber(data.value());
		if (result != null)
			return result;
		else
			return SoftCastToNumber(data.asNbt());
	}


/******************************************************************************/
/* # Natural Conversions                                                      */
/******************************************************************************/

	static public @Nullable String SoftCastToString(@Nullable Object value){
		return value instanceof String string ? string :
		       value instanceof StringTag nbt ? nbt.getAsString() :
		       value instanceof Number number ? number.toString() :
		       value instanceof NumericTag nbt ? nbt.getAsNumber().toString() :
		       value instanceof ResourceLocation id ? id.toString() :
		       value instanceof Component id ? id.getString() :
		       value instanceof ItemLore lore ? TextArrayToString(lore.lines()) :
		       null
		       ;
	};

	@Deprecated
	static public @Nullable ResourceLocation SoftCastToId(@Nullable Object value){
		return value instanceof ResourceLocation id ? id :
		       value instanceof String string ? ResourceLocation.tryParse(string) :
		       value instanceof StringTag nbt ? ResourceLocation.tryParse(nbt.getAsString()) :
		       null
		       ;
	};

	static public @Nullable Number SoftCastToNumber(@Nullable Object value){
		return value instanceof Number number ? number :
		       value instanceof NumericTag nbt ? nbt.getAsNumber() :
		       null
		       ;
	};

	static public @Nullable Tag SoftCastToNbt(@Nullable Object value){
		return value instanceof Tag nbt ? nbt :
		       value instanceof String string ? StringTag.valueOf(string) :
		       value instanceof Number number ? DoubleTag.valueOf(number.doubleValue()) :
		       value instanceof Component text ? ComponentSerialization.FLAT_CODEC.encodeStart(NbtOps.INSTANCE, text).mapOrElse(o->o, o->null) :
		       null
		       ;
	};

	@Deprecated
	static public @Nullable Component SoftCastToText(@Nullable Object value){
		return value instanceof Component text ? text :
		       value instanceof Tag nbt ? NbtToText(nbt) :
		       null
		       ;
	}


/******************************************************************************/
/* # Rich Text Util                                                           */
/******************************************************************************/

	static public Component NbtToText(@NotNull Tag nbt){
		return ComponentSerialization.FLAT_CODEC
			.parse(NbtOps.INSTANCE, nbt)
			.mapOrElse(Function.identity(), _0->null)
			;
	}

	static public List<Component> NbtToTextArray(@NotNull Tag nbt){
		return ComponentSerialization.FLAT_CODEC
			.sizeLimitedListOf(256)
			.parse(NbtOps.INSTANCE, nbt)
			.mapOrElse(Function.identity(), _0->null)
			;
	}

	static public String TextArrayToString(@NotNull List<Component> lines){
		StringBuilder builder = new StringBuilder();
		for (var l : lines) {
			builder.append(l.getString());
			builder.append('\n');
		}

		return builder.toString();
	}

}
