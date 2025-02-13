package fr.estecka.variantscit.modules;

import org.jetbrains.annotations.Nullable;
import com.mojang.serialization.DataResult;
import fr.estecka.variantscit.VariantsCitMod;
import fr.estecka.variantscit.nbt.NbtPath;
import net.minecraft.component.ComponentType;
import net.minecraft.nbt.AbstractNbtNumber;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtString;
import net.minecraft.util.Identifier;

public abstract class AArbitraryComponentModule<T>
extends ASimpleComponentCachingModule<T>
{
	public AArbitraryComponentModule(ComponentType<T> type){
		super(type);
	}

	@Override
	public Identifier GetVariantForComponent(T component){
		if (component == null)
			return null;

		NbtElement nbt = GetComponentNbt(component);

		Identifier id = this.GetVariantForNbt(nbt);
		// VariantsCitMod.LOGGER.info("component_data: {}", id);
		return id;
	}

	public abstract Identifier GetVariantForNbt(NbtElement nbt);

	private NbtElement GetComponentNbt(T component){
		DataResult<NbtElement> result = componentType.getCodec().encodeStart(NbtOps.INSTANCE, component);
		if (result.isSuccess())
			return result.getOrThrow();
		else {
			VariantsCitMod.LOGGER.error( result.error().get().message() );
			return null;
		}
	}

	protected @Nullable String GetNestedData(NbtElement nbt, String[] nbtPath){
		nbt = NbtPath.Resolve(nbt, nbtPath);
		if (nbt == null)
			return null;

		String data;
		if (nbt instanceof NbtString)
			return nbt.asString();
		else if (nbt instanceof AbstractNbtNumber number)
			return number.numberValue().toString();
		else
			return null;
	}
}
