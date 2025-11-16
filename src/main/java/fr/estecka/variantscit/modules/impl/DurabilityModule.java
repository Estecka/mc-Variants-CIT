package fr.estecka.variantscit.modules.impl;

import java.util.Optional;
import java.util.stream.Stream;
import org.jetbrains.annotations.Nullable;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.estecka.variantscit.CodecUtil;
import fr.estecka.variantscit.api.IVariantManager;
import fr.estecka.variantscit.format.properties.IntegerComponentProperty;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;

public class DurabilityModule
extends AMultiComponentCachingModule
{
	static public final MapCodec<DurabilityModule> CODEC = RecordCodecBuilder.mapCodec(builder->builder
		.group(
			Codec.BOOL.optionalFieldOf("debug", false).forGetter(o->o.debug),
			CodecUtil.IDENTIFIER_NAMESPACE.optionalFieldOf("namespace", "minecraft").forGetter(o->o.namespace),
			Codecs.NONNEGATIVE_INT.optionalFieldOf("scale").forGetter(o->o.scale)
		)
		.apply(builder, DurabilityModule::new)
	);

	private final String namespace;
	private final Optional<Integer> scale;

	public DurabilityModule (boolean debug, String namespace, Optional<Integer> scale){
		super(debug, Stream.of(IntegerComponentProperty.DAMAGE, IntegerComponentProperty.MAX_DAMAGE));
		this.namespace = namespace;
		this.scale = scale;
	}

	@Override
	public @Nullable ModelIdentifier RecomputeItemModel(ItemStack stack, IVariantManager library) {
		Integer max = stack.get(DataComponentTypes.MAX_DAMAGE);
		if (max == null)
			return null;

		int damage = stack.get(DataComponentTypes.DAMAGE);
		int durability = Math.clamp(max - damage, 0, max);

		if (scale.isPresent()){
			durability *= scale.get();
			durability += max-1; // Round up when dividing
			durability /= max;
			max = scale.get();
		}

		for (int i=durability; i<=max; ++i) {
			Identifier variantId = Identifier.of(namespace, String.valueOf(i));
			if (library.HasVariantModel(variantId))
				return library.GetVariantModel(variantId);
		}

		return null;
	}
}
