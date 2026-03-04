package fr.estecka.variantscit.itemdata.extractors.impl;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;

public class IntegerComponentProperty
extends AMonoComponentProperty<Integer,Integer>
{
	static public final IntegerComponentProperty DAMAGE     = new IntegerComponentProperty(DataComponents.DAMAGE);
	static public final IntegerComponentProperty MAX_DAMAGE = new IntegerComponentProperty(DataComponents.MAX_DAMAGE);

	public IntegerComponentProperty(DataComponentType<Integer> componentType){
		super(componentType);
	}

	@Override
	public Integer GetPropertyValue(Integer component){
		return component;
	}
}
