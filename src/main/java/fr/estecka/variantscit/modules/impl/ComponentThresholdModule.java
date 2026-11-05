package fr.estecka.variantscit.modules.impl;

import java.util.Map;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.estecka.variantscit.util.CodecUtil;
import fr.estecka.variantscit.VCitRegistries;
import fr.estecka.variantscit.commands.CommandLogger;
import fr.estecka.variantscit.itemdata.containers.IDataContainer;
import fr.estecka.variantscit.itemdata.extractors.IDataExtractor;
import fr.estecka.variantscit.modules.cache.CacheKeySet;
import fr.estecka.variantscit.modules.cache.ECachePolicy;
import fr.estecka.variantscit.modules.libraries.ILinearCitModule;
import fr.estecka.variantscit.modules.libraries.ILinearLibrary;

public class ComponentThresholdModule
implements ILinearCitModule
{
	static public final Codec<Integer> BIAS_CODEC = CodecUtil.Enum(Codec.STRING, Map.of(
		"strictly_equal",    0,
		"lesser_or_equal",  -1,
		"greater_or_equal", +1
	));

	static public final MapCodec<ComponentThresholdModule> MAPCODEC = RecordCodecBuilder.<ComponentThresholdModule>mapCodec(builder->
		builder.group(
			VCitRegistries.ITEM_PROPERTIES.mapCodec.forGetter(o->o.itemProperty),
			BIAS_CODEC.fieldOf("modelRange").forGetter(o->o.bias),
			Codec.FLOAT.optionalFieldOf("scale",  1f).forGetter(o->o.scale),
			Codec.FLOAT.optionalFieldOf("offset", 0f).forGetter(o->o.offset)
		)
		.apply(builder, ComponentThresholdModule::new)
	);

	private final IDataExtractor itemProperty;
	private final int bias;
	private final float scale;
	private final float offset;


	public ComponentThresholdModule(IDataExtractor itemProperty, int bias, float scale, float offset){
		this.itemProperty = itemProperty;
		this.bias = bias;
		this.scale = scale;
		this.offset = offset;
	}

	@Override
	public CacheKeySet GetCacheKeys() {
		return itemProperty.GetCacheKeys();
	}

	@Override
	public ECachePolicy GetCachePolicy() {
		return ECachePolicy.ALWAYS;
	}

	@Override
	public @Nullable Identifier GetItemModel(ItemStack stack, ILinearLibrary library) {
		Integer value = this.GetComponentValue(stack);
		if (value == null)
			return null;

		return library.GetWithBias((int)(value*scale + offset), bias);
	}

	@Override
	public @Nullable Identifier Walkthrough(ItemStack stack, ILinearLibrary library, CommandLogger logger) {
		Integer value = this.GetComponentValue(stack);
		logger.Info("Raw data: {}", CommandLogger.ItemData(value, "missing or invalid"));
		if (value == null)
			return null;

		value = (int)(value*scale + offset);
		logger.Info("Transformed: {}", CommandLogger.ItemData(value));
		return this.GetItemModel(stack, library);
	}

	private @Nullable Integer GetComponentValue(ItemStack stack){
		IDataContainer result = this.itemProperty.Extract(stack);
		if (result == null || !(result.asNumber() instanceof Number number))
			return null;

		return number.intValue();
	}
}
