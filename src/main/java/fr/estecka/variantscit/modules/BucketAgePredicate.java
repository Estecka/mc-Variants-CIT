package fr.estecka.variantscit.modules;

import fr.estecka.variantscit.UnclampedOverridePredicate;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

public class BucketAgePredicate
implements UnclampedOverridePredicate
{
	@Override
	public float unclampedCall(ItemStack stack, ClientWorld world, LivingEntity entity, int seed) {
		NbtComponent bucket = stack.get(DataComponentTypes.BUCKET_ENTITY_DATA);
		NbtCompound nbt;

		if (bucket == null || (nbt=bucket.getNbt()) == null || !nbt.contains("Age", NbtElement.NUMBER_TYPE))
			return 0;

		return nbt.getFloat("Age");
	}
}
