package fr.estecka.variantscit.reload;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringIdentifiable;

public enum EModuleFeature
implements StringIdentifiable
{
	ITEM_MODEL("item_model"),
	EQUIPMENT ("equippable"),
	;

	static public final Codec<EModuleFeature> CODEC = StringIdentifiable.createCodec(EModuleFeature::values);

	public final String name;

	private EModuleFeature(String name){
		this.name = name;
	}

	@Override
	public String asString() {
		return name;
	}

}
