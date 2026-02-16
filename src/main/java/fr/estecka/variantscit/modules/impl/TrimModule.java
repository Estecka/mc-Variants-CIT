package fr.estecka.variantscit.modules.impl;

import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.equipment.trim.ArmorTrim;

public class TrimModule
extends ASimpleMonoComponentModule<ArmorTrim>
{
	public TrimModule(){
		super(DataComponents.TRIM);
	}

	@Override
	public ResourceLocation GetVariantForComponent(ArmorTrim trim){
		if (trim == null)
			return null;

		ResourceLocation pattern = trim.pattern().unwrapKey().get().location();
		ResourceLocation material = trim.material().unwrapKey().get().location();

		return pattern.withSuffix("_" + material.getPath());
	}
}
