package fr.estecka.variantscit.format.properties;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import fr.estecka.variantscit.CodecUtil;
import fr.estecka.variantscit.format.NbtAdapter;
import net.minecraft.component.ComponentType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.Registries;

public record ItemComponentProperty(
	ComponentType<?> componentType,
	NbtAdapter nbtAdapter
)
implements IStringProperty
{
	static public final Codec<ItemComponentProperty> CODEC = RecordCodecBuilder.create(builder->builder
		.group(
			Registries.DATA_COMPONENT_TYPE.getCodec().fieldOf("componentType").forGetter(ItemComponentProperty::componentType),
			NbtAdapter.MAPCODEC.forGetter(ItemComponentProperty::nbtAdapter)
		)
		.apply(builder, ItemComponentProperty::new)
	);

	@Override
	public int GetPropertyHash(ItemStack stack){
		Object cmp = stack.get(this.componentType);
		return (cmp!=null) ? cmp.hashCode() : 0;
	}

	@Override
	public String GetPropertyString(ItemStack stack){
		NbtElement nbt = CodecUtil.GetComponentNbt(stack, this.componentType);
		if (nbt == null)
			return null;
		else
			return this.nbtAdapter.ResolveData(nbt);
	}
}
