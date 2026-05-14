package fr.estecka.variantscit.reload;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum EModuleHook
implements StringRepresentable
{
	ITEM_MODEL("item_model"),
	EQUIPPABLE("equippable"),
	TRIM_PATTERN("trimp_pattern"),
	;

	static public final Codec<EModuleHook> CODEC = StringRepresentable.fromEnum(EModuleHook::values);

	public final String name;

	private EModuleHook(String name){
		this.name = name;
	}

	@Override
	public String getSerializedName() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}

	static public <T> Map<EModuleHook,T> MapOf(Function<EModuleHook,T> function){
		Map<EModuleHook,T> result = new HashMap<>();
		for (EModuleHook hook : EModuleHook.values()){
			T value = function.apply(hook);
			if (value != null)
				result.put(hook, value);
		}
		return result;
	}
}
