package fr.estecka.variantscit.reload;

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

}
