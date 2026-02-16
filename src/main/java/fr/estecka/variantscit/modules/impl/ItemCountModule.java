package fr.estecka.variantscit.modules.impl;

import java.util.Collection;
import java.util.List;
import com.mojang.serialization.MapCodec;
import fr.estecka.variantscit.CodecUtil;
import fr.estecka.variantscit.format.properties.ItemCountProperty;
import fr.estecka.variantscit.modules.cache.MultiPropertyCache.ICacheablePropertySource;
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
	public Collection<ICacheablePropertySource> GetCacheSources() {
		return List.of(ItemCountProperty.UNIT);
	}

	@Override
	public ResourceLocation GetItemModel(ItemStack stack, ILinearLibrary library){
		return library.GetOrLesser(stack.getCount());
	}
}
