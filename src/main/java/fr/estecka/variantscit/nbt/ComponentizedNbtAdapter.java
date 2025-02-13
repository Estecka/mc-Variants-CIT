package fr.estecka.variantscit.nbt;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.component.ComponentType;
import net.minecraft.registry.Registries;

public record ComponentizedNbtAdapter(
	ComponentType<?> componentType,
	NbtAdapter nbtAdapter
) {
	static public final Codec<ComponentizedNbtAdapter> CODEC = RecordCodecBuilder.create(builder->builder
		.group(
			Registries.DATA_COMPONENT_TYPE.getCodec().fieldOf("componentType").forGetter(ComponentizedNbtAdapter::componentType),
			NbtAdapter.MAP_CODEC.forGetter(ComponentizedNbtAdapter::nbtAdapter)
		)
		.apply(builder, ComponentizedNbtAdapter::new)
	);
}
