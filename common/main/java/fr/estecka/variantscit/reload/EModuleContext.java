package fr.estecka.variantscit.reload;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringIdentifiable;

public enum EModuleContext
implements StringIdentifiable
{
	ITEM_MODEL("item_model"),
	EQUIPPABLE("equippable"),
	;

	static public final Codec<EModuleContext> CODEC = StringIdentifiable.createCodec(EModuleContext::values);

	public final String name;

	private EModuleContext(String name){
		this.name = name;
	}

	@Override
	public String asString() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}

}
