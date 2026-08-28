package fr.estecka.variantscit.itemdata.extractors.impl;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.decoration.painting.PaintingVariant;

public class PaintingVariantProperty
extends AMonoComponentProperty<Holder<PaintingVariant>,Identifier>
{
	static public final PaintingVariantProperty UNIT = new PaintingVariantProperty();

	private PaintingVariantProperty(){
		super(DataComponents.PAINTING_VARIANT);
	}

	@Override
	public Identifier GetPropertyValue(Holder<PaintingVariant> component) {
		return GetPropertyId(component);
	}

	public Identifier GetPropertyId(Holder<PaintingVariant> component){
		return component.unwrapKey().get().identifier();
	}
	
}
