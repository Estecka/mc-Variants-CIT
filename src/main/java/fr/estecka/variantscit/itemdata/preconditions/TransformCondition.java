package fr.estecka.variantscit.itemdata.preconditions;

import fr.estecka.variantscit.itemdata.containers.IDataContainer;
import fr.estecka.variantscit.itemdata.extractors.IDataExtractor;
import fr.estecka.variantscit.itemdata.functions.IDataFunction;
import fr.estecka.variantscit.modules.cache.CacheKeySet;
import net.minecraft.world.item.ItemStack;

public record TransformCondition(
	IDataExtractor extractor,
	IDataFunction transform
)
implements IItemPrecondition
{
	@Override
	public boolean Matches(ItemStack stack) {
		IDataContainer data = extractor.Extract(stack);
		return data != null
		    && transform.LooseTypedTransform(data) != null
		    ;
	}

	@Override
	public CacheKeySet GetCacheKeys() {
		return CacheKeySet.Of(extractor.GetCacheKey());
	}
}
