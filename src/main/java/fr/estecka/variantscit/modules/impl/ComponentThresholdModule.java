package fr.estecka.variantscit.modules.impl;

import java.util.Map;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.estecka.variantscit.CodecUtil;
import fr.estecka.variantscit.modules.cache.MultiPropertyCache;
import fr.estecka.variantscit.commands.CommandLogger;
import fr.estecka.variantscit.format.NbtPath;
import fr.estecka.variantscit.modules.libraries.ILinearLibrary;
import fr.estecka.variantscit.modules.libraries.LinearLibrary.ILinearCitModule;

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
			CodecUtil.IDENTIFIER_NAMESPACE.optionalFieldOf("namespace", "minecraft").forGetter(o->o.namespace),
			BuiltInRegistries.DATA_COMPONENT_TYPE.byNameCodec().fieldOf("componentType").forGetter(o->o.componentType),
			NbtPath.CODEC.optionalFieldOf("nbtPath", NbtPath.IDENTITY).forGetter(o->o.nbtPath),
			BIAS_CODEC.fieldOf("modelRange").forGetter(o->o.bias),
			Codec.FLOAT.optionalFieldOf("scale",  1f).forGetter(o->o.scale),
			Codec.FLOAT.optionalFieldOf("offset", 0f).forGetter(o->o.offset)
		)
		.apply(builder, ComponentThresholdModule::new)
	);

	private final String namespace;

	private final MultiPropertyCache cache;
	private final DataComponentType<?> componentType;
	private final NbtPath nbtPath;
	private final int bias;
	private final float scale;
	private final float offset;


	public ComponentThresholdModule(String namespace, DataComponentType<?> component, NbtPath path, int bias, float scale, float offset){
		this.componentType = component;
		this.namespace = namespace;
		this.nbtPath = path;
		this.bias = bias;
		this.scale = scale;
		this.offset = offset;

		this.cache = new MultiPropertyCache(false, component);
	}

	@Override
	public String GetNamespace() {
		return this.namespace;
	}

	@Override
	public @Nullable ResourceLocation GetItemModel(ItemStack stack, ILinearLibrary library) {
		return this.cache.ComputeIfAbsent(stack, _0->this.RecomputeItemModel(stack, library));
	}

	public @Nullable ResourceLocation RecomputeItemModel(ItemStack stack, ILinearLibrary library) {
		Integer value = this.GetComponentValue(stack);
		if (value == null)
			return null;

		return library.GetWithBias((int)(value*scale + offset), bias);
	}

	@Override
	public @Nullable ResourceLocation Walkthrough(ItemStack stack, ILinearLibrary library, CommandLogger logger) {
		Integer value = this.GetComponentValue(stack);
		logger.Info("Raw data: {}", CommandLogger.ItemData(value, "missing or invalid"));
		if (value == null)
			return null;

		value = (int)(value*scale + offset);
		logger.Info("Transformed: {}", CommandLogger.ItemData(value));
		return this.RecomputeItemModel(stack, library);
	}

	private @Nullable Integer GetComponentValue(ItemStack stack){
		Tag nbt = CodecUtil.GetComponentNbt(stack, componentType);
		if (nbt == null)
			return null;

		nbt = this.nbtPath.Resolve(nbt);
		if (nbt == null	||	!(nbt instanceof NumericTag number))
			return null;

		return number.getAsInt();
	}
}
