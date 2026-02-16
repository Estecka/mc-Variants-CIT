package fr.estecka.variantscit.modules.impl;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.estecka.variantscit.VariantsCitMod;
import fr.estecka.variantscit.modules.libraries.IVariantLibrary;
import fr.estecka.variantscit.commands.CommandLogger;
import fr.estecka.variantscit.format.Substitution;
import fr.estecka.variantscit.format.properties.IStringProperty;
import fr.estecka.variantscit.format.properties.TransformableProperty;

public class MultiComponentFormatModule
extends ASimpleMultiComponentCachingModule
{
	static public final MapCodec<MultiComponentFormatModule> CODEC = RecordCodecBuilder.mapCodec(builder->builder
		.group(
			Codec.BOOL.fieldOf("debug").orElse(false).forGetter(mod -> mod.debug),
			Substitution.CODEC.fieldOf("format").forGetter(m->m.format),
			ExtraCodecs.strictUnboundedMap(Substitution.VARNAME_CODEC, IStringProperty.CODEC).fieldOf("variables").forGetter(m->m.varGetters)
		)
		.apply(builder, MultiComponentFormatModule::new)
	);

	private final Substitution format;
	private final Map<String, IStringProperty> varGetters;

	public MultiComponentFormatModule(boolean debug, Substitution format, Map<String,IStringProperty> variables){
		super(debug, variables.values().toArray(IStringProperty[]::new));
		this.format = format;
		this.varGetters = Map.copyOf(variables);

		this.format.MatchWarning(variables.keySet());
	}

	@Override
	public ResourceLocation RecomputeItemVariant(ItemStack stack){
		Map<String,String> variables = new HashMap<>();

		if (debug)
			VariantsCitMod.LOGGER.info("[component_format] {}", this.format);

		for (var entry : this.varGetters.entrySet()){
			String value = entry.getValue().GetPropertyString(stack);
			if (debug)
				VariantsCitMod.LOGGER.info("\t${{}} -> {}", entry.getKey(), value);
			if (value == null)
				return null;

			variables.put(entry.getKey(), value);
		}

		String rawId = this.format.Substitute(variables);
		if (debug)
			VariantsCitMod.LOGGER.info("\t= {}", rawId);
		variables.clear();
		ResourceLocation id = ResourceLocation.tryParse(rawId);
		return id;
	}

	@Override
	public @Nullable ResourceLocation Walkthrough(ItemStack stack, IVariantLibrary library, CommandLogger logger) {
		boolean failure = false;
		Map<String,String> variables = new HashMap<>();

		logger.Info("Format: \"{}\"", CommandLogger.PackData(this.format));
		for (var entry : varGetters.entrySet()){
			String raw = TransformableProperty.GetRaw(entry.getValue()).GetPropertyString(stack);
			String transformed = entry.getValue().GetPropertyString(stack);

			logger.Info("${{}}:", CommandLogger.PackData(entry.getKey()));
			logger.Info("- Raw data: {}", CommandLogger.ItemData(raw, "Missing or invalid"));
			logger.Info("- Transformed: {}", CommandLogger.ItemData(transformed));

			failure |= (transformed == null);
			variables.put(entry.getKey(), transformed);
		}

		if (failure)
			logger.Info("Some data could not be processed.");
		else
			logger.Info("Formatted variant: {}", CommandLogger.ItemData(this.format.Substitute(variables)));

		return this.RecomputeItemModel(stack, library);
	}
}
