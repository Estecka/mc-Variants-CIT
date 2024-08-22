package fr.estecka.variantscit.modules;

import fr.estecka.variantscit.api.ISimpleCitModule;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.passive.AxolotlEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class AxolotlBucketModule
implements ISimpleCitModule
{
	@Override
	public Identifier GetItemVariant(ItemStack stack){
		NbtComponent nbt = stack.get(DataComponentTypes.BUCKET_ENTITY_DATA);
		if (!nbt.contains("Variant"))
			return null;

		int variantRaw = nbt.getNbt().getInt("Variant");
		return Identifier.of(AxolotlEntity.Variant.byId(variantRaw).getName());
	}
}
