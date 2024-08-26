package fr.estecka.variantscit.modules;

import fr.estecka.variantscit.api.ISimpleCitModule;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.passive.AxolotlEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Identifier;

public class AxolotlBucketModule
implements ISimpleCitModule
{
	@Override
	public Identifier GetItemVariant(ItemStack stack){
		NbtComponent component = stack.get(DataComponentTypes.BUCKET_ENTITY_DATA);
		NbtCompound nbt;

		if (component==null || (nbt=component.getNbt()) == null || !nbt.contains("Variant", NbtElement.NUMBER_TYPE))
			return null;

		int variantRaw = nbt.getInt("Variant");
		return Identifier.tryParse(AxolotlEntity.Variant.byId(variantRaw).getName());
	}
}
