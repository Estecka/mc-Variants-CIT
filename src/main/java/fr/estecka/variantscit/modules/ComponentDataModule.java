package fr.estecka.variantscit.modules;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.estecka.variantscit.CodecUtil;
import fr.estecka.variantscit.format.ETransform;
import fr.estecka.variantscit.format.NbtAdapter;
import net.minecraft.component.ComponentType;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class ComponentDataModule<T>
extends AArbitraryComponentModule<T>
{
	static public final MapCodec<ComponentDataModule<?>> CODEC = RecordCodecBuilder.mapCodec(builder->builder
		.group(
			Registries.DATA_COMPONENT_TYPE.getCodec().fieldOf("componentType").forGetter(o->o.componentType),
			NbtAdapter.MAPCODEC.forGetter(o->o.adapter),
			ETransform.ARRAY_CODEC.fieldOf("transforms").orElse(new ETransform[0]).forGetter(o->o.transforms),
			Codec.BOOL.fieldOf("debug").orElse(false).forGetter(o->o.debug)
		)
		.apply(builder, ComponentDataModule::new)
	);

	@Deprecated
	static public final <T> MapCodec<ComponentDataModule<?>> CreateLegacyCodec(ComponentType<T> componentType){
		return RecordCodecBuilder.mapCodec(builder->builder
			.group(
				CodecUtil.MapWithAlternative(NbtAdapter.MAPCODEC, NbtAdapter.LEGACY_MAPCODEC).forGetter(o->o.adapter),
				CodecUtil.MapWithAlternative(ETransform.ARRAY_CODEC.fieldOf("transform"), ETransform.LEGACY_CODEC.fieldOf("lowercase")).forGetter(o->o.transforms),
				Codec.BOOL.fieldOf("debug").orElse(false).forGetter(o -> o.debug)
			)
			.apply(builder, (adapter, transform, debug) -> new ComponentDataModule<T>(componentType, adapter, transform, debug))
		);
	}

	private final NbtAdapter adapter;
	private final ETransform[] transforms;

	public ComponentDataModule(ComponentType<T> type, NbtAdapter adapter, ETransform[] transforms, boolean debug){
		super(type, debug);
		this.adapter = adapter;
		this.transforms = transforms;
	}

	@Override
	public Identifier GetVariantForNbt(NbtElement nbt){
		String data = adapter.ResolveData(nbt);
		if (data == null)
			return null;

		ETransform.Transform(transforms, data);
		return Identifier.tryParse(data);
	}
}
