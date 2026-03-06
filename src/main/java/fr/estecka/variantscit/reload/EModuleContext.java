package fr.estecka.variantscit.reload;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum EModuleContext
implements StringRepresentable
{
	ITEM_MODEL("item_model"),
	EQUIPPABLE("equippable"),
	;

	static public final Codec<EModuleContext> CODEC = StringRepresentable.fromEnum(EModuleContext::values);

	public final String name;

	private EModuleContext(String name){
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

	static public <T> Map<EModuleContext,T> MapOf(Function<EModuleContext,T> function){
		Map<EModuleContext,T> result = new HashMap<>();
		for (EModuleContext ctx : EModuleContext.values()){
			T value = function.apply(ctx);
			if (value != null)
				result.put(ctx, value);
		}
		return result;
	}
}
