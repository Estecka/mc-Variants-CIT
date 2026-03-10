package fr.estecka.variantscit.modules.impl;

import java.util.Optional;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import com.mojang.serialization.MapCodec;
import fr.estecka.variantscit.modules.cache.CacheKeySet;
import fr.estecka.variantscit.modules.cache.ComponentCacheKey;
import fr.estecka.variantscit.modules.cache.ECachePolicy;
import fr.estecka.variantscit.modules.libraries.ILinearCitModule;
import fr.estecka.variantscit.modules.libraries.ILinearLibrary;


public record DurabilityModule(
	Optional<Integer> scale
)
implements ILinearCitModule
{
	static public final MapCodec<DurabilityModule> MAPCODEC = ExtraCodecs.NON_NEGATIVE_INT
		.optionalFieldOf("scale")
		.xmap(DurabilityModule::new, DurabilityModule::scale)
		;

	@Override
	public CacheKeySet GetCacheKeys() {
		return ComponentCacheKey.KeysOf(
			DataComponents.MAX_DAMAGE,
			DataComponents.DAMAGE
		);
	}
	
	@Override
	public ECachePolicy GetCachePolicy() {
		return ECachePolicy.ALWAYS;
	}

	@Override
	public @Nullable ResourceLocation GetItemModel(ItemStack stack, ILinearLibrary library) {
		Integer max = stack.get(DataComponents.MAX_DAMAGE);
		if (max == null)
			return null;

		int damage = stack.get(DataComponents.DAMAGE);
		int durability = Math.clamp(max - damage, 0, max);

		if (scale.isPresent()){
			durability *= scale.get();
			durability += max-1; // Round up when dividing
			durability /= max;
		}

		return library.GetOrGreater(durability);
	}
}
