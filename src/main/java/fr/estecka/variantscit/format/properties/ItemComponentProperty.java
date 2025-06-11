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
import net.minecraft.component.ComponentType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public record ItemComponentProperty(
	ComponentType<?> componentType,
	NbtAdapter nbtAdapter
)
implements IStringProperty
{
	static public final MapCodec<ItemComponentProperty> MAP_CODEC = RecordCodecBuilder.mapCodec(builder->builder
		.group(
			Registries.DATA_COMPONENT_TYPE.getCodec().fieldOf("componentType").forGetter(ItemComponentProperty::componentType),
			NbtAdapter.MAPCODEC.forGetter(ItemComponentProperty::nbtAdapter)
		)
		.apply(builder, ItemComponentProperty::new)
	);

	static public final Codec<TransformableProperty<ItemComponentProperty>> MONOSTRING_DECODER = Codec.STRING.flatXmap(
		ItemComponentProperty::MonostringParse,
		__->DataResult.error(()->"Encoding not supported")
	);

	@Override
	public int GetPropertyHash(ItemStack stack){
		Object cmp = stack.get(this.componentType);
		return (cmp!=null) ? cmp.hashCode() : 0;
	}

	@Override
	public Object GetReference(ItemStack stack) {
		return stack.get(this.componentType);
	}

	@Override
	public String GetPropertyString(ItemStack stack){
		NbtElement nbt = CodecUtil.GetComponentNbt(stack, this.componentType);
		if (nbt == null)
			return null;

		String result = this.nbtAdapter.ResolveData(nbt);
		return result;
	}

	static private DataResult<TransformableProperty<ItemComponentProperty>> MonostringParse(String input){
		int pathLocation;
		for (pathLocation = 0; pathLocation<input.length(); pathLocation++){
			char c = input.charAt(pathLocation);
			if (c == '.' || !Identifier.isCharValid(c))
				break;
		}

		String s_component = input.substring(0, pathLocation);
		String s_path      = input.substring(pathLocation);

		var r_path = (!s_path.isEmpty()) ? NbtPath.Parse(s_path) : DataResult.success(NbtPath.IDENTITY);
		var r_component = Registries.DATA_COMPONENT_TYPE.getCodec().decode(NbtOps.INSTANCE, NbtString.of(s_component)).map(Pair::getFirst);

		if (r_path.isError())
			return r_path.map(__->null);

		if (r_component.isError())
			return r_component.map(__->null);

		NbtPath path = r_path.getOrThrow();
		ComponentType<?> component = r_component.getOrThrow();

		return DataResult.success(new TransformableProperty<>(
			new ItemComponentProperty(component, new NbtAdapter(path, INbtInput.AUTO)),
			IStringTransform.AUTO,
			Optional.empty()
		));
	}
}
