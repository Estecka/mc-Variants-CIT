package fr.estecka.variantscit.format.properties;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.passive.AxolotlEntity;

public class AxolotlVariantProperty
extends AMonoComponentProperty<AxolotlEntity.Variant>
{
	static public final AxolotlVariantProperty UNIT = new AxolotlVariantProperty();

	public AxolotlVariantProperty(){
		super(DataComponentTypes.AXOLOTL_VARIANT);
	}

	@Override
	public String GetPropertyString(AxolotlEntity.Variant component) {
		return component.getId();
	}
}
