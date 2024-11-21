package fr.estecka.variantscit.modules;

import org.jetbrains.annotations.Nullable;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.render.item.property.numeric.NumericProperty;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

public class EnchantedBookLevelPredicate
implements NumericProperty
{
	static public final MapCodec<EnchantedBookLevelPredicate> CODEC = MapCodec.unit(new EnchantedBookLevelPredicate());

	@Override
	public float getValue(ItemStack stack, @Nullable ClientWorld world, LivingEntity entity, int seed){
		ItemEnchantmentsComponent enchants = stack.get(DataComponentTypes.STORED_ENCHANTMENTS);
		if (enchants == null || enchants.isEmpty())
			return 0;
		else
			return enchants.getEnchantmentEntries().iterator().next().getIntValue();
	}

	public MapCodec<EnchantedBookLevelPredicate> getCodec(){
		return CODEC;
	}
}
