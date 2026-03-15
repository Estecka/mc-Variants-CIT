package fr.estecka.variantscit.itemdata.preconditions;

import java.util.List;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import fr.estecka.variantscit.CodecUtil;
import fr.estecka.variantscit.VCitRegistries;
import fr.estecka.variantscit.modules.cache.CacheKeySet;
import net.minecraft.world.item.ItemStack;

public record ConditionList(
	List<IItemPrecondition> conditions,
	boolean groupType
)
implements IItemPrecondition
{
	static public final boolean MATCHANY_TYPE = true;
	static public final boolean MATCHALL_TYPE = false;

	static public final Codec<ConditionList> MATCHANY_CODEC = TypedCodec(MATCHANY_TYPE);
	static public final Codec<ConditionList> MATCHALL_CODEC = TypedCodec(MATCHALL_TYPE);

	static public final MapCodec<ConditionList> MATCHANY_MAPCODEC = MATCHALL_CODEC.fieldOf("any");
	static public final MapCodec<ConditionList> MATCHALL_MAPCODEC = MATCHALL_CODEC.fieldOf("all");

	/**
	 * @implNote Use {@link Codec#lazyInitialized} to  workaround  the  circular
	 * dependency with {@link IItemPrecondition#MONOSTRINGMAP_CODEC}
	 */
	static private Codec<ConditionList> TypedCodec(boolean type){
		return CodecUtil.WithAlternative(
			VCitRegistries.PRECONDITIONS.codec.listOf(),
			Codec.lazyInitialized(()->IItemPrecondition.MONOSTRINGMAP_CODEC)
		)
		.xmap(list->new ConditionList(list, type), ConditionList::conditions)
		;
	}

	@Override
	public boolean Matches(ItemStack stack) {
		for (IItemPrecondition c : conditions)
			if (groupType == c.Matches(stack))
				return groupType;

		return !groupType;
	}
	
	@Override
	public CacheKeySet GetCacheKeys() {
		return CacheKeySet.OfList(this.conditions);
	}
}
