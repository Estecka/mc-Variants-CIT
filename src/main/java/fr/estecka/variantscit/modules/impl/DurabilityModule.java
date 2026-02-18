package fr.estecka.variantscit.modules.impl;

import java.util.Optional;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.estecka.variantscit.CodecUtil;
import fr.estecka.variantscit.modules.cache.CacheKeySet;
import fr.estecka.variantscit.modules.cache.ComponentCacheKey;
import fr.estecka.variantscit.modules.libraries.ILinearLibrary;
import fr.estecka.variantscit.modules.libraries.LinearLibrary.ILinearCitModule;

public class DurabilityModule
implements ILinearCitModule
{
	static public final MapCodec<DurabilityModule> CODEC = RecordCodecBuilder.mapCodec(builder->builder
		.group(
			CodecUtil.IDENTIFIER_NAMESPACE.optionalFieldOf("namespace", "minecraft").forGetter(DurabilityModule::GetNamespace),
			ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("scale").forGetter(o->o.scale)
		)
		.apply(builder, DurabilityModule::new)
	);

	private final String namespace;
	private final Optional<Integer> scale;

	public DurabilityModule (String namespace, Optional<Integer> scale){
		this.namespace = namespace;
		this.scale = scale;
	}

	@Override
	public String GetNamespace() {
		return namespace;
	}

	@Override
	public CacheKeySet GetCacheKeys() {
		return ComponentCacheKey.KeysOf(
			DataComponents.MAX_DAMAGE,
			DataComponents.DAMAGE
		);
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
