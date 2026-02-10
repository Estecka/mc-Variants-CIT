package fr.estecka.variantscit.format.properties;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;

public class IntegerComponentProperty
extends AMonoComponentProperty<Integer>
{
	static public final IntegerComponentProperty DAMAGE     = new IntegerComponentProperty(DataComponents.DAMAGE);
	static public final IntegerComponentProperty MAX_DAMAGE = new IntegerComponentProperty(DataComponents.MAX_DAMAGE);

	public IntegerComponentProperty(DataComponentType<Integer> componentType){
		super(componentType);
	}

	@Override
	public String GetPropertyString(Integer component){
		return component.toString();
	}
}
