package fr.estecka.variantscit.modules;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import fr.estecka.variantscit.VariantsCitMod;
import net.minecraft.component.ComponentType;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.Identifier;

public abstract class AArbitraryComponentModule<T>
extends ASimpleComponentCachingModule<T>
{
	private final Codec<T> componentCodec;
	protected final boolean debug;

	public AArbitraryComponentModule(ComponentType<T> type, boolean debug){
		super(type);
		this.componentCodec = type.getCodecOrThrow();
		this.debug = debug;
	}

	@Override
	public Identifier GetVariantForComponent(T component){
		if (component == null)
			return null;

		NbtElement nbt = GetComponentNbt(component);

		Identifier id = this.GetVariantForNbt(nbt);
		if (debug)
			VariantsCitMod.LOGGER.info("component_data: {} -> {}", componentType, id);
		return id;
	}

	public abstract Identifier GetVariantForNbt(NbtElement nbt);

	private NbtElement GetComponentNbt(T component){
		DataResult<NbtElement> result = componentCodec.encodeStart(NbtOps.INSTANCE, component);
		if (result.isSuccess())
			return result.getOrThrow();
		else {
			VariantsCitMod.LOGGER.error( result.error().get().message() );
			return null;
		}
	}
}
