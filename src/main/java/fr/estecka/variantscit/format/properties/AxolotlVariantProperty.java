package fr.estecka.variantscit.format.properties;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.passive.AxolotlEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

public class AxolotlVariantProperty
extends AMonoComponentProperty<NbtComponent>
{
	public AxolotlVariantProperty(){
		super(DataComponentTypes.BUCKET_ENTITY_DATA);
	}

	@Override
	public String GetPropertyString(NbtComponent component) {
		NbtCompound nbt;
		if ((nbt=component.getNbt()) == null || !nbt.contains("Variant", NbtElement.NUMBER_TYPE))
			return null;

		int variantRaw = nbt.getInt("Variant");
		return AxolotlEntity.Variant.byId(variantRaw).getName();
	}
}
