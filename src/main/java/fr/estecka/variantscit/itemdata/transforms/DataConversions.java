package fr.estecka.variantscit.itemdata.transforms;

import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import fr.estecka.variantscit.util.CodecUtil;
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
		"rich_text_array", DataConversions::StrictRichTextArray,
		"snbt",            DataConversions::StrictSnbt
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
		return StricIdentifier(input, Identifier.DEFAULT_NAMESPACE);
	}

	static public IDataContainer StricIdentifier(IDataContainer input, String defaultNamespace) {
		if (input.value() instanceof Identifier)
			return input;
		if (input.value() instanceof String string)
			return RawDataContainer.<Identifier>OfNullable(CodecUtil.NamespacedIdentifier(defaultNamespace, string).mapOrElse(o->o, o->null));
		if (input.asNbt() instanceof StringTag nbt)
			return RawDataContainer.<Identifier>OfNullable(CodecUtil.NamespacedIdentifier(defaultNamespace, nbt.getAsString()).mapOrElse(o->o, o->null));
		return null;
	}

	static public IDataContainer StrictString(IDataContainer input) {
		if (input.value() instanceof String)
			return input;
		if (input.value() instanceof Identifier id)
			return RawDataContainer.<String>OfNullable(id.toString());
		if (input.asNbt() instanceof StringTag nbt)
			return RawDataContainer.<String>OfNullable(nbt.getAsString());
		return null;
	}

	static public IDataContainer StrictNumber(IDataContainer input) {
		if (input.value() instanceof Number)
			return input;
		if (input.asNbt() instanceof NumericTag nbt)
			return RawDataContainer.<Number>OfNullable(nbt.getAsNumber());
		return null;
	}

	static public IDataContainer StrictRichText(IDataContainer input) {
		if (input.value() instanceof Component)
			return input;
		
		var nbt = input.asNbt();
		if (nbt != null)
			return RawDataContainer.<Component>OfNullable(NbtToText(nbt));
		else
			return null;
	}

	static public IDataContainer StrictRichTextArray(IDataContainer input) {
		if (input.value() instanceof ItemLore)
			return input;
		
		var nbt = input.asNbt();
		if (nbt != null)
			return RawDataContainer.<List<Component>>OfNullable(NbtToTextArray(nbt));
		else
			return null;
	}

	static public IDataContainer StrictNbt(IDataContainer input) {
		if (input.value() instanceof Tag)
			return input;
		
		return RawDataContainer.<Tag>OfNullable(input.asNbt());
	}

	static public IDataContainer StrictSnbt(IDataContainer input) {
		input = StrictNbt(input);
		if (input == null)
			return null;

		Tag nbt = (Tag)input.value();
		String snbt = nbt.getAsString();
		return RawDataContainer.<String>OfNullable(snbt);
	}

	static public IDataContainer GetRgb(IDataContainer input) {
		return FormatRgb(ParseColor(input));
	}

	static public IDataContainer GetArgb(IDataContainer input) {
		return FormatArgb(ParseColor(input));
	}

	static public IDataContainer GetRgba(IDataContainer input) {
		return FormatRgba(ParseColor(input));
	}

	static public IDataContainer GetHex(IDataContainer input) {
		return FormatHex(ParseColor(input));
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

		result = SoftCastToString(nbt);
		if (result != null)
			return result;

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
		       value instanceof StringTag nbt ? Identifier.tryParse(nbt.getAsString()) :
		       null
		       ;
	};

	static public @Nullable Number SoftCastToNumber(@Nullable Object value){
		return value instanceof Number number ? number :
		       value instanceof NumericTag nbt ? nbt.getAsNumber() :
		       null
		       ;
	};

	static private final long DEFAULT_ALPHA = 0xFF000000L;

	static private record ParsedColor(long value, boolean hasAlpha) {}

	static private @Nullable ParsedColor ParseColor(IDataContainer input) {
		Object value = input.value();
		if (value instanceof Number number)
			return ParseNumberColor(number);
		if (value instanceof String string)
			return ParseColor(string);
		return ParseColor(input.asString());
	}

	static private @Nullable ParsedColor ParseColor(@Nullable String input) {
		if (input == null)
			return null;

		input = input.trim();
		if (input.isEmpty())
			return null;

		var named = ParseNamedColor(input);
		if (named != null)
			return new ParsedColor(DEFAULT_ALPHA | named, false);

		if (input.startsWith("#"))
			return ParseHexColor(input.substring(1));

		if (input.startsWith("0x") || input.startsWith("0X"))
			return ParseHexColor(input.substring(2));

		if (input.matches("[0-9]+"))
			return ParseDecimalColor(input);

		if (input.matches("(?i)[0-9a-f]{6,8}"))
			return ParseHexColor(input);

		return null;
	}

	static private @Nullable ParsedColor ParseNumberColor(Number input) {
		long value = Integer.toUnsignedLong(input.intValue());
		long raw = value & 0xFFFFFFFFL;
		boolean hasAlpha = (value > 0xFFFFFFL) || (value < 0);
		return new ParsedColor(raw, hasAlpha);
	}

	static private @Nullable ParsedColor ParseDecimalColor(String input) {
		try {
			long value = Long.parseLong(input);
			long raw = value & 0xFFFFFFFFL;
			boolean hasAlpha = (value > 0xFFFFFFL) || (value < 0);
			return new ParsedColor(raw, hasAlpha);
		}
		catch (NumberFormatException ex) {
			return null;
		}
	}

	static private @Nullable ParsedColor ParseHexColor(String input) {
		if (input.length() > 8)
			return null;

		try {
			long value = Long.parseLong(input, 16) & 0xFFFFFFFFL;
			if (input.length() <= 6)
				return new ParsedColor(DEFAULT_ALPHA | value, false);
			return new ParsedColor(value, true);
		}
		catch (NumberFormatException ex) {
			return null;
		}
	}

	static private @Nullable Long ParseNamedColor(String input) {
		switch (input.toLowerCase(Locale.ROOT)) {
			case "black":         return 0x000000L;
			case "dark_blue":     return 0x0000AAL;
			case "dark_green":    return 0x00AA00L;
			case "dark_aqua":     return 0x00AAAAL;
			case "dark_red":      return 0xAA0000L;
			case "dark_purple":   return 0xAA00AAL;
			case "gold":          return 0xFFAA00L;
			case "gray":          return 0xAAAAAAL;
			case "dark_gray":     return 0x555555L;
			case "blue":          return 0x5555FFL;
			case "green":         return 0x55FF55L;
			case "aqua":          return 0x55FFFFL;
			case "red":           return 0xFF5555L;
			case "light_purple":  return 0xFF55FFL;
			case "yellow":        return 0xFFFF55L;
			case "white":         return 0xFFFFFFL;
			default:               return null;
		}
	}

	static private long NormalizeArgb(ParsedColor color) {
		long value = color.value & 0xFFFFFFFFL;
		if (color.hasAlpha)
			return value;
		else
			return DEFAULT_ALPHA | (value & 0xFFFFFFL);
	}

	static private @Nullable RawDataContainer<String> FormatRgb(@Nullable ParsedColor color) {
		if (color == null)
			return null;

		return RawDataContainer.OfNullable(String.format(Locale.ROOT, "#%06X", NormalizeArgb(color) & 0xFFFFFFL));
	}

	static private @Nullable RawDataContainer<String> FormatArgb(@Nullable ParsedColor color) {
		if (color == null)
			return null;

		return RawDataContainer.OfNullable(String.format(Locale.ROOT, "#%08X", NormalizeArgb(color)));
	}

	static private @Nullable RawDataContainer<String> FormatRgba(@Nullable ParsedColor color) {
		if (color == null)
			return null;

		long argb = NormalizeArgb(color);
		return RawDataContainer.OfNullable(String.format(Locale.ROOT, "#%06X%02X", argb & 0xFFFFFFL, (argb >>> 24) & 0xFFL));
	}

	static private @Nullable RawDataContainer<String> FormatHex(@Nullable ParsedColor color) {
		if (color == null)
			return null;

		return RawDataContainer.OfNullable(String.format(Locale.ROOT, "0x%08X", NormalizeArgb(color)));
	}

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
