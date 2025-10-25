package fr.estecka.variantscit.modules;

import java.util.Optional;
import java.util.stream.Stream;
import org.jetbrains.annotations.Nullable;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.estecka.variantscit.CodecUtil;
import fr.estecka.variantscit.MultiPropertyCache;
import fr.estecka.variantscit.format.properties.IntegerComponentProperty;
import fr.estecka.variantscit.modulebakers.LinearLibrary;
import fr.estecka.variantscit.modulebakers.LinearLibrary.ILinearCitModule;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;

public class DurabilityModule
implements ILinearCitModule
{
	static public final MapCodec<DurabilityModule> CODEC = RecordCodecBuilder.mapCodec(builder->builder
		.group(
			Codec.BOOL.optionalFieldOf("debug", false).forGetter(o->o.cache.debug),
			CodecUtil.IDENTIFIER_NAMESPACE.optionalFieldOf("namespace", "minecraft").forGetter(DurabilityModule::GetNamespace),
			Codecs.NON_NEGATIVE_INT.optionalFieldOf("scale").forGetter(o->o.scale)
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
	public @Nullable Identifier GetItemModel(ItemStack stack, LinearLibrary library) {
		return this.cache.ComputeIfAbsent(stack, __->RecomputeItemModel(stack, library));
	}

	public @Nullable Identifier RecomputeItemModel(ItemStack stack, LinearLibrary library) {
		Integer max = stack.get(DataComponentTypes.MAX_DAMAGE);
		if (max == null)
			return null;

		int damage = stack.get(DataComponentTypes.DAMAGE);
		int durability = Math.clamp(max - damage, 0, max);

		if (scale.isPresent()){
			durability *= scale.get();
			durability += max-1; // Round up when dividing
			durability /= max;
		}

		return library.GetOrGreater(durability);
	}
}
