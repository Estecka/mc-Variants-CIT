package fr.estecka.variantscit.modules.impl;

import java.util.Map;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.estecka.variantscit.util.CodecUtil;
import fr.estecka.variantscit.commands.CommandLogger;
import fr.estecka.variantscit.itemdata.transforms.impl.NbtPath;
import fr.estecka.variantscit.modules.cache.CacheKeySet;
import fr.estecka.variantscit.modules.cache.ComponentCacheKey;
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
			BuiltInRegistries.DATA_COMPONENT_TYPE.byNameCodec().fieldOf("componentType").forGetter(o->o.componentType),
			NbtPath.CODEC.optionalFieldOf("nbtPath", NbtPath.IDENTITY).forGetter(o->o.nbtPath),
			BIAS_CODEC.fieldOf("modelRange").forGetter(o->o.bias),
			Codec.FLOAT.optionalFieldOf("scale",  1f).forGetter(o->o.scale),
			Codec.FLOAT.optionalFieldOf("offset", 0f).forGetter(o->o.offset)
		)
		.apply(builder, ComponentThresholdModule::new)
	);

	private final DataComponentType<?> componentType;
	private final NbtPath nbtPath;
	private final int bias;
	private final float scale;
	private final float offset;


	public ComponentThresholdModule(DataComponentType<?> component, NbtPath path, int bias, float scale, float offset){
		this.componentType = component;
		this.nbtPath = path;
		this.bias = bias;
		this.scale = scale;
		this.offset = offset;
	}

	@Override
	public CacheKeySet GetCacheKeys() {
		return ComponentCacheKey.KeysOf(componentType);
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
		Tag nbt = CodecUtil.GetComponentNbt(stack, componentType);
		if (nbt == null)
			return null;

		nbt = this.nbtPath.Resolve(nbt);
		if (nbt == null	||	!(nbt instanceof NumericTag number))
			return null;

		return number.intValue();
	}
}
