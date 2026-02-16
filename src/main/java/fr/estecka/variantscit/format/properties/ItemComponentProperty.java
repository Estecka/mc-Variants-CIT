package fr.estecka.variantscit.format.properties;

import java.util.Optional;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.estecka.variantscit.CodecUtil;
import fr.estecka.variantscit.format.INbtInput;
import fr.estecka.variantscit.format.IStringTransform;
import fr.estecka.variantscit.format.NbtAdapter;
import fr.estecka.variantscit.format.NbtPath;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;


public class ItemComponentProperty<T>
extends AMonoComponentProperty<T>
{
	static public final MapCodec<ItemComponentProperty<?>> MAP_CODEC = RecordCodecBuilder.mapCodec(builder->builder
		.group(
			BuiltInRegistries.DATA_COMPONENT_TYPE.byNameCodec().fieldOf("componentType").forGetter(o->o.source.componentType()),
			NbtAdapter.MAPCODEC.forGetter(o->o.nbtAdapter)
		)
		.apply(builder, ItemComponentProperty::new)
	);

	static public final Codec<TransformableProperty<ItemComponentProperty<?>>> MONOSTRING_DECODER = Codec.STRING.flatXmap(
		ItemComponentProperty::MonostringParse,
		__->DataResult.error(()->"Encoding not supported")
	);

	public final NbtAdapter nbtAdapter;

	public ItemComponentProperty(DataComponentType<T> type, NbtAdapter adapter){
		super(type);
		this.nbtAdapter = adapter;
	}

	@Override
	public String GetPropertyString(T component){
		Tag nbt = CodecUtil.GetComponentNbt(component, source.componentType().codec());
		if (nbt == null)
			return null;

		String result = this.nbtAdapter.ResolveData(nbt);
		return result;
	}

	static private DataResult<TransformableProperty<ItemComponentProperty<?>>> MonostringParse(String input){
		int pathLocation;
		for (pathLocation = 0; pathLocation<input.length(); pathLocation++){
			char c = input.charAt(pathLocation);
			if (c == '.' || !ResourceLocation.isAllowedInResourceLocation(c))
				break;
		}

		String s_component = input.substring(0, pathLocation);
		String s_path      = input.substring(pathLocation);

		var r_path = (!s_path.isEmpty()) ? NbtPath.Parse(s_path) : DataResult.success(NbtPath.IDENTITY);
		var r_component = BuiltInRegistries.DATA_COMPONENT_TYPE.byNameCodec().decode(NbtOps.INSTANCE, StringTag.valueOf(s_component)).map(Pair::getFirst);

		if (r_path.isError())
			return r_path.map(__->null);

		if (r_component.isError())
			return r_component.map(__->null);

		NbtPath path = r_path.getOrThrow();
		DataComponentType<?> component = r_component.getOrThrow();

		return DataResult.success(new TransformableProperty<>(
			new ItemComponentProperty<>(component, new NbtAdapter(path, INbtInput.AUTO)),
			IStringTransform.SANITIZE_AUTO,
			Optional.empty()
		));
	}
}
