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
import net.minecraft.resources.ResourceLocation;

@FunctionalInterface
public interface IDataConversions<T>
extends IDataTransform
{
	abstract T flatLooseTransform(Object value);

	@Override
	default IDataContainer LooseTypedTransform(IDataContainer input) {
		return (input != null) ? RawDataContainer.OfNullable(flatLooseTransform(input)) : null;
	}


	static public final Codec<IDataTransform> EXPECT_UNIT_CODEC = CodecUtil.Enum(Codec.STRING, Map.of(
		"string",          IDataConversions::StrictString,
		"identifier",      IDataConversions::StricIdentifier,
		"number",          IDataConversions::StrictNumber,
		"rich_text",       IDataConversions::StrictRichText,
		"rich_text_array", IDataConversions::StrictRichTextArray
	));

	static public final Codec<IDataTransform> EXPECT_GROUP_CODEC = CodecUtil.OneOrMany(EXPECT_UNIT_CODEC)
		.xmap(AlternativeTransform::Wrap, AlternativeTransform::Unwrap)
		;

	static public IDataConversions<String> AGGRESSIVE_TO_STRING = Alternatives(
		IDataConversions::SoftCastToString,
		o->SoftCastToString(SoftCastToText(o)),
		o->SoftCastToString(LooseCastTextArray(o))
	);

	@SafeVarargs
	static public <T> IDataConversions<T> Alternatives(Function<Object,T>... functions){
		return value -> {
			for (var f : functions){
				T result = f.apply(value);
				if (result != null) return result;
			}
			return null;
		};
	}


/******************************************************************************/
/* # "Expect" field                                                           */
/******************************************************************************/

static public IDataContainer StricIdentifier(IDataContainer input) {
	if (input.value() instanceof ResourceLocation id)
		return input;
	if (input.value() instanceof String string)
		return RawDataContainer.OfNullable(ResourceLocation.tryParse(string));
	if (input.asNbt() instanceof StringTag nbt)
		return RawDataContainer.OfNullable(ResourceLocation.tryParse(nbt.getAsString()));
	return null;
}

static public IDataContainer StrictString(IDataContainer input) {
	if (input.value() instanceof String)
		return input;
	if (input.value() instanceof ResourceLocation id)
		return RawDataContainer.OfNullable(input.asString());
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
	if (input.value() instanceof Component)
		return input;
	
	var nbt = input.asNbt();
	if (nbt != null)
		return RawDataContainer.OfNullable(NbtToText(nbt));
	else
		return null;
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
		       null
		       ;
	};

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
		       null
		       ;
	};

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

	static public String LooseCastTextArray(Object value){
		if (value instanceof String s)
			return s;

		var lines = value instanceof Tag nbt ? NbtToTextArray(nbt) : null;
		return (lines != null) ? TextArrayToString(lines) : null;
	}
}
