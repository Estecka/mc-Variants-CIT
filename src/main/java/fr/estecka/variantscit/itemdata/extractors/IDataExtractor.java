package fr.estecka.variantscit.itemdata.extractors;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import fr.estecka.variantscit.VCitRegistries;
import fr.estecka.variantscit.itemdata.containers.IDataContainer;
import fr.estecka.variantscit.itemdata.preconditions.IItemPrecondition;
import fr.estecka.variantscit.modules.cache.CacheKeySet;
import fr.estecka.variantscit.modules.cache.ICacheKey;
import net.minecraft.world.item.ItemStack;

public interface IDataExtractor
extends ICacheKey.Keyable, IItemPrecondition
{
	static public final Codec<IDataExtractor> CODEC = VCitRegistries.ITEM_PROPERTIES.codec;
	static public final MapCodec<IDataExtractor> MAPCODEC = VCitRegistries.ITEM_PROPERTIES.mapCodec;

	IDataContainer Extract(ItemStack stack);

	@Override
	default boolean Matches(ItemStack stack) {
		return this.Extract(stack) != null;
	}

	@Override
	default CacheKeySet GetCacheKeys() {
		return CacheKeySet.Of(this.GetCacheKey());
	}
}
