package fr.estecka.variantscit.itemdata.extractors.impl;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.decoration.PaintingVariant;

public class PaintingVariantProperty
extends AMonoComponentProperty<Holder<PaintingVariant>,ResourceLocation>
{
	static public final PaintingVariantProperty UNIT = new PaintingVariantProperty();

	private PaintingVariantProperty(){
		super(DataComponents.PAINTING_VARIANT);
	}

	@Override
	public ResourceLocation GetPropertyValue(Holder<PaintingVariant> component) {
		return GetPropertyId(component);
	}

	public ResourceLocation GetPropertyId(Holder<PaintingVariant> component){
		return component.unwrapKey().get().location();
	}
	
}
