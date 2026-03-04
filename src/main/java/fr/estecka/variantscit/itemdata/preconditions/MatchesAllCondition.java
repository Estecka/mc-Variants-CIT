package fr.estecka.variantscit.itemdata.preconditions;

import java.util.List;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import fr.estecka.variantscit.VCitRegistries;
import fr.estecka.variantscit.modules.cache.CacheKeySet;
import net.minecraft.world.item.ItemStack;

public record MatchesAllCondition(
	List<IItemPrecondition> conditions
)
implements IItemPrecondition
{
	static public final Codec<MatchesAllCondition> LITERAL_CODEC = Codec.withAlternative(IItemPrecondition.MONOSTRINGMAP_CODEC, VCitRegistries.PRECONDITIONS.codec.listOf())
		.xmap(MatchesAllCondition::new, MatchesAllCondition::conditions)
		;

	static public final MapCodec<MatchesAllCondition> MAPCODEC = LITERAL_CODEC.fieldOf("all");

	@Override
	public boolean Matches(ItemStack stack) {
		for (IItemPrecondition c : conditions)
			if (!c.Matches(stack))
				return false;

		return true;
	}
	
	@Override
	public CacheKeySet GetCacheKeys() {
		return CacheKeySet.OfList(this.conditions);
	}
}
