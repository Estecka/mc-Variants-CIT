package fr.estecka.variantscit.modules;

import java.util.List;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.estecka.variantscit.VariantsCitMod;
import net.minecraft.component.ComponentType;
import net.minecraft.nbt.AbstractNbtNumber;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class ComponentDataModule<T>
extends ASimpleComponentCachingModule<T>
{
	static public final MapCodec<ComponentDataModule<?>> CODEC = RecordCodecBuilder.mapCodec(builder->builder
		.group(
			Registries.DATA_COMPONENT_TYPE.getCodec().fieldOf("componentType").forGetter(mod -> mod.componentType),
			Codec.STRING.listOf().fieldOf("nbtPath").forGetter(s->List.of(s.path))
		)
		.apply(builder, (type,path) -> new ComponentDataModule(type, path))
	);

	private final String[] path;

	public ComponentDataModule(ComponentType<T> type, List<String> path){
		super(type);
		this.path = path.toArray(i->new String[i]);
	}

	@Override
	public Identifier GetVariantForComponent(T component){
		if (component == null)
			return null;

		NbtElement nbt = GetComponentNbt(component);
		if (nbt == null)
			return null;

		nbt = Resolve(nbt);
		if (nbt == null)
			return null;

		Identifier id = GetVariantFromData(nbt);
		// VariantsCitMod.LOGGER.info("component_data: {}", id);
		return id;
	}

	private NbtElement GetComponentNbt(T component){
		DataResult<NbtElement> result = componentType.getCodec().encodeStart(NbtOps.INSTANCE, component);
		if (result.isSuccess())
			return result.getOrThrow();
		else {
			VariantsCitMod.LOGGER.error( result.error().get().message() );
			return null;
		}
	}

	private NbtElement Resolve(NbtElement nbt){
		for (int i=0; i<path.length; ++i)
		if  (nbt instanceof NbtCompound compound)
			nbt = compound.get(path[i]);
		else
			return null;

		return nbt;
	}

	private Identifier GetVariantFromData(NbtElement nbt){
		String data;
		if (nbt instanceof NbtString)
			data = nbt.asString();
		else if (nbt instanceof AbstractNbtNumber number)
			data = number.numberValue().toString();
		else
			return null;
		
		// if (!caseSensitive)
		// 	data = data.toLowerCase();

		return Identifier.tryParse(data);
	}
}
