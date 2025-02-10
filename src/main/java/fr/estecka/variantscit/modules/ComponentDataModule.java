package fr.estecka.variantscit.modules;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.estecka.variantscit.NbtPath;
import fr.estecka.variantscit.VariantsCitMod;
import net.minecraft.component.ComponentType;
import net.minecraft.nbt.AbstractNbtNumber;
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
			NbtPath.CODEC.fieldOf("nbtPath").forGetter(s->s.path)
		)
		.apply(builder, (type,path) -> new ComponentDataModule(type, path))
	);

	private final String[] path;

	public ComponentDataModule(ComponentType<T> type, String[] path){
		super(type);
		this.path = path;
	}

	@Override
	public Identifier GetVariantForComponent(T component){
		if (component == null)
			return null;

		NbtElement nbt = GetComponentNbt(component);
		if (nbt == null)
			return null;

		nbt = NbtPath.Resolve(nbt, this.path);
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
