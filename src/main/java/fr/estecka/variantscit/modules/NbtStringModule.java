package fr.estecka.variantscit.modules;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import fr.estecka.variantscit.CodecUtil;
import fr.estecka.variantscit.VariantsCitMod;
import fr.estecka.variantscit.nbt.NbtAdapter;
import net.minecraft.component.ComponentType;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Identifier;

public class NbtStringModule
extends ASimpleComponentCachingModule<NbtComponent>
{
	static public final MapCodec<NbtStringModule> CreateCodec(ComponentType<NbtComponent> componentType){
		return CodecUtil.MapWithAlternative(
			NbtAdapter.MAPCODEC,
			NbtAdapter.LEGACY_MAPCODEC.validate(_0 -> {
				VariantsCitMod.LOGGER.warn("The custom_data parameters `nbtKey` and `caseSensitive` are being deprecated. Use `nbtPath` and `filter` instead.");
				return DataResult.success(_0);
			})
		).xmap((adpt)->new NbtStringModule(componentType,adpt), mod->mod.nbtAdapter);
	}

	private final NbtAdapter nbtAdapter;

	private NbtStringModule(ComponentType<NbtComponent> dataType, NbtAdapter nbtAdapter)
	throws IllegalStateException
	{
		super(dataType);
		this.nbtAdapter = nbtAdapter;
	}

	@Override
	public Identifier GetVariantForComponent(NbtComponent component){
		NbtElement nbt;
		if (component==null || (nbt=component.getNbt())==null)
			return null;

		String rawVariant = this.nbtAdapter.ResolveData(nbt);
		return Identifier.tryParse(rawVariant);
	}
}
