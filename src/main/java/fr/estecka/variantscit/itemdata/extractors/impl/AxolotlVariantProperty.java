package fr.estecka.variantscit.itemdata.extractors.impl;

import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.animal.axolotl.Axolotl.Variant;

public class AxolotlVariantProperty
extends AMonoComponentProperty<Axolotl.Variant,Identifier>
{
	static public final AxolotlVariantProperty UNIT = new AxolotlVariantProperty();

	public AxolotlVariantProperty(){
		super(DataComponents.AXOLOTL_VARIANT);
	}

	@Override
	public Identifier GetPropertyValue(Variant component) {
		return Identifier.tryParse(component.getName());
	}
}
