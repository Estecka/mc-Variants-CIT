package fr.estecka.variantscit.modules;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.equipment.trim.ArmorTrim;
import net.minecraft.util.Identifier;

public class TrimModule
extends ASimpleComponentCachingModule<ArmorTrim>
{
	public TrimModule(){
		super(DataComponentTypes.TRIM);
	}

	@Override
	public Identifier GetVariantForComponent(ArmorTrim trim){
		if (trim == null)
			return null;

		Identifier pattern = trim.pattern().getKey().get().getValue();
		Identifier material = trim.material().getKey().get().getValue();

		return pattern.withSuffixedPath("_" + material.getPath());
	}
}
