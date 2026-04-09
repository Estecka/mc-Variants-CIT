package fr.estecka.variantscit.modules.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.estecka.variantscit.modules.cache.CacheKeySet;
import fr.estecka.variantscit.modules.cache.ECachePolicy;
import fr.estecka.variantscit.modules.libraries.ISimpleCitModule;
import fr.estecka.variantscit.modules.libraries.IVariantLibrary;
import fr.estecka.variantscit.CodecUtil;
import fr.estecka.variantscit.VCitRegistries;
import fr.estecka.variantscit.commands.CommandLogger;
import fr.estecka.variantscit.format.Substitution;
import fr.estecka.variantscit.itemdata.containers.IDataContainer;
import fr.estecka.variantscit.itemdata.extractors.IDataExtractor;
import fr.estecka.variantscit.itemdata.extractors.TransformableExtractor;
import fr.estecka.variantscit.itemdata.transforms.IStringTransform;
import fr.estecka.variantscit.itemdata.transforms.impl.LogTransform;

public class MultiComponentFormatModule
implements ISimpleCitModule
{
	static public final Codec<TransformableExtractor<IDataExtractor>> SANITIZED_MONOSTRING_DECODER = IDataExtractor.MONOSTRING_DECODER.xmap(
		inner -> new TransformableExtractor<>(inner, IStringTransform.SANITIZE, Optional.empty()),
		TransformableExtractor::inner
	);

	static public final MapCodec<MultiComponentFormatModule> MAPCODEC = RecordCodecBuilder.mapCodec(builder->builder
		.group(
			Substitution.CODEC.fieldOf("format").forGetter(m->m.format),
			ExtraCodecs.strictUnboundedMap(
				Substitution.VARNAME_CODEC,
				CodecUtil.WithAlternative(VCitRegistries.ITEM_PROPERTIES.codec, SANITIZED_MONOSTRING_DECODER)
			).fieldOf("variables").forGetter(m->m.varGetters)
		)
		.apply(builder, MultiComponentFormatModule::new)
	);

	private final Substitution format;
	private final Map<String, IDataExtractor> varGetters;

	public MultiComponentFormatModule(Substitution format, Map<String,IDataExtractor> variables){
		this.format = format;
		this.varGetters = Map.copyOf(variables);

		this.format.MatchWarning(variables.keySet());
	}

	@Override
	public CacheKeySet GetCacheKeys() {
		return CacheKeySet.Of(varGetters.values().stream().map(IDataExtractor::GetCacheKey));
	}

	@Override
	public ECachePolicy GetCachePolicy() {
		return ECachePolicy.ALWAYS;
	}

	@Override
	public @Nullable ResourceLocation GetItemVariant(ItemStack stack) {
		Map<String,String> variables = new HashMap<>();

		for (var entry : this.varGetters.entrySet()){
			String value = IDataContainer.NullableAsString(entry.getValue().Extract(stack));
			if (value == null)
				return null;

			variables.put(entry.getKey(), value);
		}

		String rawId = this.format.Substitute(variables);
		variables.clear();
		ResourceLocation variantId = ResourceLocation.tryParse(rawId);
		return variantId;
	}

	@Override
	public @Nullable ResourceLocation Walkthrough(ItemStack stack, IVariantLibrary library, CommandLogger logger) {
		boolean failure = false;
		Map<String,String> variables = new HashMap<>();

		logger.Info("Format: \"{}\"", CommandLogger.PackData(this.format));
		for (var entry : varGetters.entrySet()){
			logger.Info("${{}}:", CommandLogger.PackData(entry.getKey()));

			IDataContainer raw = TransformableExtractor.Unwrap(entry.getValue()).Extract(stack);
			logger.Info("- Raw data: {}", CommandLogger.ItemData(raw, "Missing or invalid"));

			IDataContainer transformed = LogTransform.WithLogger(logger, ()->entry.getValue().Extract(stack));
			logger.Info("- Transformed: {}", CommandLogger.ItemData(transformed));

			failure |= (transformed == null);
			variables.put(entry.getKey(), IDataContainer.NullableAsString(transformed));
		}

		if (failure)
			logger.Info("Some data could not be processed.");
		else
			logger.Info("Formatted variant: {}", CommandLogger.ItemData(this.format.Substitute(variables)));

		return this.GetItemModel(stack, library);
	}
}
