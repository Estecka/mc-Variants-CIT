package fr.estecka.variantscit.modules.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.jetbrains.annotations.Nullable;
import fr.estecka.variantscit.modules.cache.MultiPropertyCache.ICacheablePropertySource;
import fr.estecka.variantscit.modules.libraries.IVariantCitModule;
import fr.estecka.variantscit.modules.libraries.IVariantLibrary;
import fr.estecka.variantscit.commands.CommandLogger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public record FallbackModule(IVariantCitModule... innerQueue)
implements IVariantCitModule
{
	@Override
	public @Nullable ResourceLocation GetItemModel(ItemStack stack, IVariantLibrary modelProvider) {
		for (IVariantCitModule m : innerQueue){
			ResourceLocation result = m.GetItemModel(stack, modelProvider);
			if (result != null) return result;
		}

		return null;
	}

	@Override
	public @Nullable ResourceLocation Walkthrough(ItemStack stack, IVariantLibrary library, CommandLogger logger) {
		for (IVariantCitModule m : innerQueue){
			ResourceLocation result = m.Walkthrough(stack, library, logger);
			if (result != null) return result;
		}

		return null;
	}

	@Override
	public Collection<ICacheablePropertySource> GetCacheSources() {
		Set<ICacheablePropertySource> set = new HashSet<>();
		for(IVariantCitModule m : innerQueue)
			set.addAll(m.GetCacheSources());
		return set;
	}
}
