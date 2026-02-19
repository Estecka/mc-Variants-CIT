package fr.estecka.variantscit.modules.impl;

import com.mojang.serialization.MapCodec;
import fr.estecka.variantscit.CodecUtil;
import fr.estecka.variantscit.format.properties.ItemCountProperty;
import fr.estecka.variantscit.modules.cache.CacheKeySet;
import fr.estecka.variantscit.modules.cache.ECachePolicy;
import fr.estecka.variantscit.modules.libraries.ILinearLibrary;
import fr.estecka.variantscit.modules.libraries.LinearLibrary.ILinearCitModule;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public record ItemCountModule(String namespace)
implements ILinearCitModule
{
	static public final MapCodec<ItemCountModule> CODEC = CodecUtil.IDENTIFIER_NAMESPACE
		.optionalFieldOf("namespace", "minecraft")
		.xmap(ItemCountModule::new, ItemCountModule::GetNamespace)
		;

	@Override
	public String GetNamespace() {
		return namespace;
	}

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
