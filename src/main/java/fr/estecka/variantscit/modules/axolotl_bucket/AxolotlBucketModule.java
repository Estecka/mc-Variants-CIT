package fr.estecka.variantscit.modules.axolotl_bucket;

import fr.estecka.variantscit.VariantManager;
import fr.estecka.variantscit.ModuleDefinition;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.passive.AxolotlEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class AxolotlBucketModule
extends VariantManager
{
	public AxolotlBucketModule(ModuleDefinition definition){
		super(definition);
	}

	@Override
	public Identifier GetItemVariant(ItemStack stack){
		NbtComponent nbt = stack.get(DataComponentTypes.BUCKET_ENTITY_DATA);
		if (!nbt.contains("Variant"))
			return null;

		int variantRaw = nbt.getNbt().getInt("Variant");
		return Identifier.of(AxolotlEntity.Variant.byId(variantRaw).getName());
	}
}
