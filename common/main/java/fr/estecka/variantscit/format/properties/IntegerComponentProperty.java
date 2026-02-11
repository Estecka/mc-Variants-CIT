package fr.estecka.variantscit.format.properties;

import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;

public class IntegerComponentProperty
extends AMonoComponentProperty<Integer>
{
	static public final IntegerComponentProperty DAMAGE     = new IntegerComponentProperty(DataComponentTypes.DAMAGE);
	static public final IntegerComponentProperty MAX_DAMAGE = new IntegerComponentProperty(DataComponentTypes.MAX_DAMAGE);

	public IntegerComponentProperty(ComponentType<Integer> componentType){
		super(componentType);
	}

	@Override
	public String GetPropertyString(Integer component){
		return component.toString();
	}
}
