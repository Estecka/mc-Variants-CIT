package fr.estecka.variantscit.itemdata.extractors.impl;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.DyedItemColor;

public class DyedColorProperty
extends AMonoComponentProperty<DyedItemColor, Number>
{
	static public final DyedColorProperty UNIT = new DyedColorProperty();

	public DyedColorProperty(){
		super(DataComponents.DYED_COLOR);
	}

	@Override
	public Number GetPropertyValue(DyedItemColor component) {
		return component.rgb();
	}
}
