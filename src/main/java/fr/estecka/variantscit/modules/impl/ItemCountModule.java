package fr.estecka.variantscit.modules.impl;

import fr.estecka.variantscit.itemdata.extractors.impl.ItemCountProperty;
import fr.estecka.variantscit.modules.cache.CacheKeySet;
import fr.estecka.variantscit.modules.cache.ECachePolicy;
import fr.estecka.variantscit.modules.libraries.ILinearCitModule;
import fr.estecka.variantscit.modules.libraries.ILinearLibrary;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public record ItemCountModule()
implements ILinearCitModule
{
	static public final ItemCountModule UNIT = new ItemCountModule();

	@Override
	public CacheKeySet GetCacheKeys() {
		return CacheKeySet.Of(ItemCountProperty.UNIT);
	}

	@Override
	public ECachePolicy GetCachePolicy() {
		return ECachePolicy.AVOID;
	}

	@Override
	public ResourceLocation GetItemModel(ItemStack stack, ILinearLibrary library){
		return library.GetOrLesser(stack.getCount());
	}
}
