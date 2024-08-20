package fr.estecka.variantscit.modules.enchanted_book;

import org.jetbrains.annotations.Nullable;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import fr.estecka.variantscit.UnclampedOverridePredicate;

public class EnchantedBookLevelPredicate
implements UnclampedOverridePredicate
{
	@Override
	public float unclampedCall(ItemStack stack, @Nullable ClientWorld world, LivingEntity entity, int seed){
		ItemEnchantmentsComponent enchants = stack.get(DataComponentTypes.STORED_ENCHANTMENTS);
		if (enchants == null || enchants.isEmpty())
			return 0;
		else
			return enchants.getEnchantmentEntries().iterator().next().getIntValue();
	}
}
