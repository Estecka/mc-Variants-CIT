package fr.estecka.variantscit.reload;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringIdentifiable;

public enum EModuleAspect
implements StringIdentifiable
{
	ITEM_MODEL("item_model"),
	EQUIPPABLE("equippable"),
	;

	static public final Codec<EModuleAspect> CODEC = StringIdentifiable.createCodec(EModuleAspect::values);

	public final String name;

	private EModuleAspect(String name){
		this.name = name;
	}

	@Override
	public String asString() {
		return name;
	}

}
