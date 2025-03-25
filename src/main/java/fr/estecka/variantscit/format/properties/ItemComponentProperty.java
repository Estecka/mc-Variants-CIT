package fr.estecka.variantscit.format.properties;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.estecka.variantscit.CodecUtil;
import fr.estecka.variantscit.format.ETransform;
import fr.estecka.variantscit.format.NbtAdapter;
import net.minecraft.component.ComponentType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.Registries;

public record ItemComponentProperty(
	ComponentType<?> componentType,
	NbtAdapter nbtAdapter,
	ETransform[] transforms
)
implements IStringProperty
{
	static public final MapCodec<ItemComponentProperty> MAP_CODEC = RecordCodecBuilder.mapCodec(builder->builder
		.group(
			Registries.DATA_COMPONENT_TYPE.getCodec().fieldOf("componentType").forGetter(ItemComponentProperty::componentType),
			NbtAdapter.MAPCODEC.forGetter(ItemComponentProperty::nbtAdapter),
			ETransform.ARRAY_CODEC.fieldOf("transform").orElse(ETransform.EMPTY).forGetter(ItemComponentProperty::transforms)
		)
		.apply(builder, ItemComponentProperty::new)
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
		if (result != null)
			result = ETransform.Transform(this.transforms, result);
		return result;
	}
}
