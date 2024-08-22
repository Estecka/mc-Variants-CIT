package fr.estecka.variantscit.modules.potion_effect;

import fr.estecka.variantscit.api.IVariantProvider;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class PotionTypeModule
implements IVariantProvider
{
	@Override
	public Identifier GetItemVariant(ItemStack stack){
		PotionContentsComponent potionComponent = stack.get(DataComponentTypes.POTION_CONTENTS);
		if (potionComponent == null || potionComponent.potion().isEmpty())
			return null;

		return potionComponent.potion().get().getKey().get().getValue();
	}
}
