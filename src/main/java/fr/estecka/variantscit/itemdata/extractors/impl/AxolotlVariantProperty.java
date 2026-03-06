package fr.estecka.variantscit.itemdata.extractors.impl;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.item.component.CustomData;

public class AxolotlVariantProperty
extends AMonoComponentProperty<CustomData,String>
{
	static public final AxolotlVariantProperty UNIT = new AxolotlVariantProperty();

	public AxolotlVariantProperty(){
		super(DataComponents.BUCKET_ENTITY_DATA);
	}

	@Override
	public String GetPropertyValue(CustomData component) {
		CompoundTag nbt;
		if ((nbt=component.copyTag()) == null || !nbt.contains("Variant", Tag.TAG_ANY_NUMERIC))
			return null;

		int variantRaw = nbt.getInt("Variant");
		return Axolotl.Variant.byId(variantRaw).getName();
	}
}
