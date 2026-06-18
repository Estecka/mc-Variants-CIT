package fr.estecka.variantscit.trims;

import java.util.Optional;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.estecka.variantscit.ReloadableRepository;
import net.minecraft.resources.ResourceLocation;

/**
 * @implNote decal is currently not suppported. It's listed here as a placeholder.
 */
public record TrimPatternOverlay(
	ResourceLocation assetId,
	Optional<Boolean> isDecal
) {
	static public final MapCodec<TrimPatternOverlay> MAPCODEC = RecordCodecBuilder.mapCodec(builder->
		builder.group(
			ResourceLocation.CODEC.fieldOf("asset_id").forGetter(TrimPatternOverlay::assetId),
			Codec.BOOL.optionalFieldOf("decal").forGetter(TrimPatternOverlay::isDecal)
		)
		.apply(builder, TrimPatternOverlay::new)
	);

	static public final ReloadableRepository<TrimPatternOverlay> REPOSITORY = new ReloadableRepository<>(MAPCODEC.codec(), "variants-cit/trim_pattern", "json");
	static public final Codec<TrimPatternOverlay> UNIT_CODEC = REPOSITORY.UNIT_CODEC;
	
}
