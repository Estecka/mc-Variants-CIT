package fr.estecka.variantscit.format.properties;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.util.Identifier;

public class PaintingVariantProperty
extends AMonoComponentProperty<NbtComponent>
{
	static public final PaintingVariantProperty UNIT = new PaintingVariantProperty();

	private PaintingVariantProperty(){
		super(DataComponentTypes.ENTITY_DATA);
	}

	@Override
	public String GetPropertyString(NbtComponent component) {
		return component.getNbt().getString("variant");
	}

	public Identifier GetPropertyId(NbtComponent component){
		String rawVariant = GetPropertyString(component);
		return (rawVariant!=null) ?  Identifier.tryParse(rawVariant) : null;
	}
	
}
