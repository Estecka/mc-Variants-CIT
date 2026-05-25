package fr.estecka.variantscit.modules.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.resources.Identifier;
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
import fr.estecka.variantscit.itemdata.containers.RawDataContainer;
import fr.estecka.variantscit.itemdata.extractors.IDataExtractor;
import fr.estecka.variantscit.itemdata.extractors.TransformableExtractor;
import fr.estecka.variantscit.itemdata.transforms.DataConversions;
import fr.estecka.variantscit.itemdata.transforms.IDataTransform;
import fr.estecka.variantscit.itemdata.transforms.IStringTransform;
import fr.estecka.variantscit.itemdata.transforms.SuccessiveTransform;
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
			).fieldOf("variables").forGetter(m->m.varGetters),
			SuccessiveTransform.CODEC.optionalFieldOf("transform", IDataTransform.NOOP).forGetter(m->m.transform)
		)
		.apply(builder, MultiComponentFormatModule::new)
	);

	private final Substitution format;
	private final IDataTransform transform;
	private final Map<String, IDataExtractor> varGetters;

	public MultiComponentFormatModule(Substitution format, Map<String,IDataExtractor> variables, IDataTransform transform){
		this.format = format;
		this.transform = transform;
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
	public @Nullable Identifier GetItemVariant(ItemStack stack) {
		Map<String,String> variables = new HashMap<>();

		for (var entry : this.varGetters.entrySet()){
			String value = IDataContainer.NullableAsString(entry.getValue().Extract(stack));
			if (value == null)
				return null;

			variables.put(entry.getKey(), value);
		}

		IDataContainer result = this.transform.LooseTypedTransform(RawDataContainer.<String>OfNullable(this.format.Substitute(variables)));
		variables.clear();
		if (result == null)
			return null;

		result = DataConversions.StricIdentifier(result);
		if (result == null)
			return null;
		else
			return (Identifier)result.value();
	}

	@Override
	public @Nullable Identifier Walkthrough(ItemStack stack, IVariantLibrary library, CommandLogger logger) {
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

		String substResult = this.format.Substitute(variables);
		logger.Info("Format result: {}", CommandLogger.ItemData(substResult));

		if (this.transform != IDataTransform.NOOP){
			var r = LogTransform.WithLogger(logger, ()->this.transform.LooseTypedTransform(RawDataContainer.<String>OfNullable(substResult)));
			logger.Info("Transformed format: {}", CommandLogger.ItemData(r));
		}


		return this.GetItemModel(stack, library);
	}
}
