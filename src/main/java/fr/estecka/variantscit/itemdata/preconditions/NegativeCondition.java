package fr.estecka.variantscit.itemdata.preconditions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.estecka.variantscit.CodecUtil;
import fr.estecka.variantscit.modules.cache.CacheKeySet;
import net.minecraft.world.item.ItemStack;

public record NegativeCondition(
	IItemPrecondition inner
)
implements IItemPrecondition
{
	static public MapCodec<IItemPrecondition> CodecOf(MapCodec<? extends IItemPrecondition> inner){
		return RecordCodecBuilder.mapCodec(builder->
			builder.group(
				Codec.BOOL.optionalFieldOf("negate", false).forGetter(CodecUtil.NoGetter("NegativeCondition")),
				inner.forGetter(CodecUtil.NoGetter("NegativeCondition"))
			)
			.apply(builder, NegativeCondition::Wrap)
		);
	}

	static private IItemPrecondition Wrap(boolean isNegative, IItemPrecondition inner){
		if (!isNegative)
			return inner;
		else if (inner instanceof NegativeCondition n)
			return n.inner;
		else
			return new NegativeCondition(inner);
	}

	@Override
	public boolean Matches(ItemStack stack) {
		return !inner.Matches(stack);
	}

	@Override
	public CacheKeySet GetCacheKeys() {
		return inner.GetCacheKeys();
	}
}
