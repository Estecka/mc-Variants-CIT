package fr.estecka.variantscit.itemdata.preconditions;

import java.util.ArrayList;
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

	static public final Codec<IItemPrecondition> MATCHANY_CODEC = TypedCodec(MATCHANY_TYPE);
	static public final Codec<IItemPrecondition> MATCHALL_CODEC = TypedCodec(MATCHALL_TYPE);

	static public final MapCodec<IItemPrecondition> MATCHANY_MAPCODEC = MATCHANY_CODEC.fieldOf("any");
	static public final MapCodec<IItemPrecondition> MATCHALL_MAPCODEC = MATCHALL_CODEC.fieldOf("all");

	static private Codec<IItemPrecondition> TypedCodec(boolean type){
		return CodecUtil.WithAlternative(
			VCitRegistries.PRECONDITIONS.codec.listOf(),
			MonostringConditionBuilder.MAP_CODEC
		)
		.xmap(list->ConditionList.Wrap(list, type), cond->ConditionList.Unwrap(cond, type))
		;
	}

	static public IItemPrecondition Wrap(List<IItemPrecondition> list, boolean type){
		List<IItemPrecondition> result = new ArrayList<>();

		for (IItemPrecondition cond : list) {
			if (cond instanceof ConditionList wrapper && wrapper.groupType == type)
				result.addAll(wrapper.conditions);
			else
				result.add(cond);
		}

		if (result.size() == 1)
			return result.getFirst();
		else
			return new ConditionList(result, type);
	}

	static public List<IItemPrecondition> Unwrap(IItemPrecondition cond, boolean type){
		if (cond instanceof ConditionList list && list.groupType == type)
			return list.conditions;
		else
			return List.of(cond);
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
