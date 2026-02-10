package fr.estecka.variantscit.modules.impl;

import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.estecka.variantscit.CodecUtil;
import fr.estecka.variantscit.MultiPropertyCache;
import fr.estecka.variantscit.commands.CommandLogger;
import fr.estecka.variantscit.format.properties.IntegerComponentProperty;
import fr.estecka.variantscit.modules.libraries.ILinearLibrary;
import fr.estecka.variantscit.modules.libraries.LinearLibrary.ILinearCitModule;

public class DurabilityModule
implements ILinearCitModule
{
	static public final MapCodec<DurabilityModule> CODEC = RecordCodecBuilder.mapCodec(builder->builder
		.group(
			Codec.BOOL.optionalFieldOf("debug", false).forGetter(o->o.cache.debug),
			CodecUtil.IDENTIFIER_NAMESPACE.optionalFieldOf("namespace", "minecraft").forGetter(DurabilityModule::GetNamespace),
			ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("scale").forGetter(o->o.scale)
		)
		.apply(builder, DurabilityModule::new)
	);

	private final MultiPropertyCache cache;
	private final String namespace;
	private final Optional<Integer> scale;

	public DurabilityModule (boolean debug, String namespace, Optional<Integer> scale){
		this.cache = new MultiPropertyCache(debug, Stream.of(IntegerComponentProperty.DAMAGE, IntegerComponentProperty.MAX_DAMAGE));
		this.namespace = namespace;
		this.scale = scale;
	}

	@Override
	public String GetNamespace() {
		return namespace;
	}

	@Override
	public @Nullable ResourceLocation GetItemModel(ItemStack stack, ILinearLibrary library) {
		return this.cache.ComputeIfAbsent(stack, __->RecomputeItemModel(stack, library));
	}

	public @Nullable ResourceLocation RecomputeItemModel(ItemStack stack, ILinearLibrary library) {
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

	@Override
	public @Nullable ResourceLocation Walkthrough(ItemStack stack, ILinearLibrary library, CommandLogger logger) {
		return this.RecomputeItemModel(stack, library);
	}
}
