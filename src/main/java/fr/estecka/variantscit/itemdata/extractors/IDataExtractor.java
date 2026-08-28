package fr.estecka.variantscit.itemdata.extractors;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import fr.estecka.variantscit.util.CodecUtil;
import fr.estecka.variantscit.VCitRegistries;
import fr.estecka.variantscit.itemdata.containers.IDataContainer;
import fr.estecka.variantscit.itemdata.extractors.impl.ItemComponentProperty;
import fr.estecka.variantscit.itemdata.preconditions.IItemPrecondition;
import fr.estecka.variantscit.modules.cache.CacheKeySet;
import fr.estecka.variantscit.modules.cache.ICacheKey;
import net.minecraft.world.item.ItemStack;

public interface IDataExtractor
extends ICacheKey.Keyable, IItemPrecondition
{
	static public final MapCodec<IDataExtractor> MAPCODEC = VCitRegistries.ITEM_PROPERTIES.mapCodec;

	static public final Codec<IDataExtractor> MONOSTRING_DECODER = CodecUtil.WithAlternative(
		VCitRegistries.ITEM_PROPERTIES.unitCodec,
		ItemComponentProperty.MONOSTRING_DECODER
	);

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
