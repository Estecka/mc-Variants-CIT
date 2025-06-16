package fr.estecka.variantscit.reload;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringIdentifiable;

public enum EModuleFeature
implements StringIdentifiable
{
	ITEM("item"),
	EQUIPMENT("equipment"),
	;

	static public final Codec<EModuleFeature> CODEC = StringIdentifiable.createCodec(EModuleFeature::values);

	public final String prefix;

	private EModuleFeature(String prefix){
		this.prefix = prefix;
	}

	@Override
	public String asString() {
		return prefix;
	}

}
