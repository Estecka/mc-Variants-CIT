package fr.estecka.variantscit.format.properties;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.decoration.painting.PaintingVariant;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public class PaintingVariantProperty
extends AMonoComponentProperty<RegistryEntry<PaintingVariant>>
{
	static public final PaintingVariantProperty UNIT = new PaintingVariantProperty();

	private PaintingVariantProperty(){
		super(DataComponentTypes.PAINTING_VARIANT);
	}

	@Override
	public String GetPropertyString(RegistryEntry<PaintingVariant> component) {
		return GetPropertyId(component).toString();
	}

	public Identifier GetPropertyId(RegistryEntry<PaintingVariant> component){
		return component.getKey().get().getValue();
	}
	
}
