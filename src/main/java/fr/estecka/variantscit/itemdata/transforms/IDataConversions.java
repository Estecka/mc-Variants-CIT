package fr.estecka.variantscit.itemdata.transforms;

import java.util.List;
import java.util.function.Function;
import org.jetbrains.annotations.Nullable;
import com.mojang.serialization.Codec;
import fr.estecka.variantscit.CodecUtil;
import fr.estecka.variantscit.VCitRegistries;
import fr.estecka.variantscit.itemdata.containers.IDataContainer;
import fr.estecka.variantscit.itemdata.containers.RawDataContainer;
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



	@Deprecated static public final Codec<IDataConversions<?>> LEGACY_CODEC = VCitRegistries.NBT_INPUTS.codec;
	@Deprecated static public final Codec<IDataConversions<?>[]> LEGACY_ARRAY_CODEC = CodecUtil.OneOrMany(CODEC).xmap(list->list.toArray(IDataConversions[]::new), array->List.of(array));
	@Deprecated static public final Codec<IDataConversions<?>> LEGACY_GROUP_CODEC = LEGACY_ARRAY_CODEC.xmap(IDataConversions::Grouped, type -> type instanceof Group group ? group.content() : new IDataConversions[]{ type } );

	static public IDataConversions<String> TO_STRING = Alternatives(
		IDataConversions::SoftCastToString,
		o->SoftCastToString(SoftCastToText(o)),
		o->SoftCastToString(LooseCastTextArray(o))
	);

	@Deprecated
	public record Group(IDataConversions<?>... content)
	implements IDataConversions<Object>
	{
		@Override
		public Object flatLooseTransform(Object value) {
			for (int i=0; i<content.length; ++i){
				Object result = content[i].flatLooseTransform(value);
				if (result != null) return result;
			}
			return null;
		}

	}

	@Deprecated
	static public IDataConversions<?> Grouped(IDataConversions<?>... group){
		if (group.length == 1)
			return group[0];
		else
			return new Group(group);
	}


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
	static public Component NbtToText(Tag nbt){
		return ComponentSerialization.FLAT_CODEC
			.parse(NbtOps.INSTANCE, nbt)
			.mapOrElse(Function.identity(), _0->null)
			;
	}

	static public List<Component> NbtToTextArray(Tag nbt){
		return ComponentSerialization.FLAT_CODEC
			.sizeLimitedListOf(256)
			.parse(NbtOps.INSTANCE, nbt)
			.mapOrElse(Function.identity(), _0->null)
			;
	}

	static public String TextArrayToString(List<Component> lines){
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
