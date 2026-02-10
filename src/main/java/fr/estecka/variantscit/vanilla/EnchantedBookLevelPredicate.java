package fr.estecka.variantscit.vanilla;

import org.jetbrains.annotations.Nullable;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public class EnchantedBookLevelPredicate
implements RangeSelectItemModelProperty
{
	static public final MapCodec<EnchantedBookLevelPredicate> CODEC = MapCodec.unit(new EnchantedBookLevelPredicate());

	@Override
	public float get(ItemStack stack, @Nullable ClientLevel world, LivingEntity entity, int seed){
		ItemEnchantments enchants = stack.get(DataComponents.STORED_ENCHANTMENTS);
		if (enchants == null || enchants.isEmpty())
			return 0;
		else
			return enchants.entrySet().iterator().next().getIntValue();
	}

	public MapCodec<EnchantedBookLevelPredicate> type(){
		return CODEC;
	}
}
