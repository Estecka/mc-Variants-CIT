package fr.estecka.variantscit.modules;

import com.mojang.serialization.Codec;
import fr.estecka.variantscit.CodecUtil;
import fr.estecka.variantscit.VariantsCitMod;
import net.minecraft.component.ComponentType;
import net.minecraft.nbt.NbtElement;
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

		NbtElement nbt = CodecUtil.GetComponentNbt(component, componentCodec);
		if (nbt == null)
			return null;

		Identifier id = this.GetVariantForNbt(nbt);
		if (debug)
			VariantsCitMod.LOGGER.info("component_data: {} -> {}", componentType, id);
		return id;
	}

	public abstract Identifier GetVariantForNbt(NbtElement nbt);
}
