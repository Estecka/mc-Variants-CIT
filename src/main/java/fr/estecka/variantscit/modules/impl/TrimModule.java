package fr.estecka.variantscit.modules.impl;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.trim.ArmorTrim;
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

		Identifier pattern = trim.getPattern().getKey().get().getValue();
		Identifier material = trim.getMaterial().getKey().get().getValue();

		return pattern.withSuffixedPath("_" + material.getPath());
	}
}
