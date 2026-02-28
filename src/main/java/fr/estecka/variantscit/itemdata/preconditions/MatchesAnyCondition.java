package fr.estecka.variantscit.itemdata.preconditions;

import java.util.List;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import fr.estecka.variantscit.VCitRegistries;
import fr.estecka.variantscit.modules.cache.CacheKeySet;
import net.minecraft.world.item.ItemStack;

public record MatchesAnyCondition(
	List<IItemPrecondition> conditions
)
implements IItemPrecondition
{
	static public final Codec<MatchesAnyCondition> ARRAY_CODEC = VCitRegistries.PRECONDITIONS.codec
		.listOf(1, Integer.MAX_VALUE)
		.xmap(MatchesAnyCondition::new, MatchesAnyCondition::conditions)
		;

	static public final MapCodec<MatchesAnyCondition> MAPCODEC = ARRAY_CODEC.fieldOf("any");

	@Override
	public boolean Matches(ItemStack stack) {
		for (IItemPrecondition c : conditions)
			if (c.Matches(stack))
				return true;

		return false;
	}
	
	@Override
	public CacheKeySet GetCacheKeys() {
		return CacheKeySet.OfList(this.conditions);
	}
}
