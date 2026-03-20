package fr.estecka.variantscit.itemdata.transforms;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.mojang.serialization.Codec;
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
import net.minecraft.resources.Identifier;
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


/******************************************************************************/
/* # Strict Casts                                                             */
/******************************************************************************/

static public IDataContainer StricIdentifier(IDataContainer input) {
	if (input.value() instanceof Identifier)
		return input;
	if (input.value() instanceof String string)
		return RawDataContainer.OfNullable(Identifier.tryParse(string));
	if (input.asNbt() instanceof StringTag nbt)
		return RawDataContainer.OfNullable(Identifier.tryParse(nbt.value()));
	return null;
}

static public IDataContainer StrictString(IDataContainer input) {
	if (input.value() instanceof String)
		return input;
	if (input.value() instanceof Identifier id)
		return RawDataContainer.OfNullable(id.toString());
	if (input.asNbt() instanceof StringTag nbt)
		return RawDataContainer.OfNullable(nbt.value());
	return null;
}

static public IDataContainer StrictNumber(IDataContainer input) {
	if (input.value() instanceof Number)
		return input;
	if (input.asNbt() instanceof NumericTag nbt)
		return RawDataContainer.OfNullable(nbt.asNumber());

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

		result =  SoftCastToNumber(data.asNbt());

		return result;
	}


/******************************************************************************/
/* # Natural Conversions                                                      */
/******************************************************************************/

	static public @Nullable String SoftCastToString(@Nullable Object value){
		return value instanceof String string ? string :
		       value instanceof StringTag nbt ? nbt.value() :
		       value instanceof Number number ? number.toString() :
		       value instanceof NumericTag nbt ? nbt.asNumber().get().toString() :
		       value instanceof Identifier id ? id.toString() :
		       value instanceof Component id ? id.getString() :
		       value instanceof ItemLore lore ? TextArrayToString(lore.lines()) :
		       null
		       ;
	};

	@Deprecated
	static public @Nullable Identifier SoftCastToId(@Nullable Object value){
		return value instanceof Identifier id ? id :
		       value instanceof String string ? Identifier.tryParse(string) :
		       value instanceof StringTag nbt ? Identifier.tryParse(nbt.value()) :
		       null
		       ;
	};

	static public @Nullable Number SoftCastToNumber(@Nullable Object value){
		return value instanceof Number number ? number :
		       value instanceof NumericTag nbt ? nbt.asNumber().get() :
		       null
		       ;
	};

	static public @Nullable Tag SoftCastToNbt(@Nullable Object value){
		return value instanceof Tag nbt ? nbt :
		       value instanceof String string ? StringTag.valueOf(string) :
		       value instanceof Number number ? DoubleTag.valueOf(number.doubleValue()) :
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
		return ComponentSerialization.CODEC
			.parse(NbtOps.INSTANCE, nbt)
			.mapOrElse(Function.identity(), _0->null)
			;
	}

	static public List<Component> NbtToTextArray(@NotNull Tag nbt){
		return ComponentSerialization.CODEC
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
