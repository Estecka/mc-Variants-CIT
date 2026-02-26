package fr.estecka.variantscit.itemdata.extractors.impl;

import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.component.CustomData;

public class PaintingVariantProperty
extends AMonoComponentProperty<CustomData,String>
{
	static public final PaintingVariantProperty UNIT = new PaintingVariantProperty();

	private PaintingVariantProperty(){
		super(DataComponents.ENTITY_DATA);
	}

	@Override
	public String GetPropertyString(CustomData component) {
		return component.getUnsafe().getString("variant");
	}

	public ResourceLocation GetPropertyId(CustomData component){
		String rawVariant = GetPropertyString(component);
		return (rawVariant!=null) ?  ResourceLocation.tryParse(rawVariant) : null;
	}
	
}
