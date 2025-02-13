package fr.estecka.variantscit.modules;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.estecka.variantscit.nbt.NbtAdapter;
import net.minecraft.component.ComponentType;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class ComponentDataModule<T>
extends AArbitraryComponentModule<T>
{
	static public final MapCodec<ComponentDataModule<?>> CODEC = RecordCodecBuilder.mapCodec(builder->builder
		.group(
			Registries.DATA_COMPONENT_TYPE.getCodec().fieldOf("componentType").forGetter(mod -> mod.componentType),
			NbtAdapter.MAP_CODEC.forGetter(m->m.adapter)
		)
		.apply(builder, ComponentDataModule::new)
	);

	private final NbtAdapter adapter;

	public ComponentDataModule(ComponentType<T> type, NbtAdapter adapter){
		super(type);
		this.adapter = adapter;
	}

	@Override
	public Identifier GetVariantForNbt(NbtElement nbt){
		String data = adapter.ResolveData(nbt);
		if (data == null)
			return null;

		return Identifier.tryParse(data);
	}
}
