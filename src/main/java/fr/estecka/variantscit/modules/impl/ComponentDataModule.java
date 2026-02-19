package fr.estecka.variantscit.modules.impl;

import java.util.Optional;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.estecka.variantscit.CodecUtil;
import fr.estecka.variantscit.modules.cache.CacheKeySet;
import fr.estecka.variantscit.modules.cache.ECachePolicy;
import fr.estecka.variantscit.modules.libraries.ISimpleCitModule;
import fr.estecka.variantscit.modules.libraries.IVariantLibrary;
import fr.estecka.variantscit.commands.CommandLogger;
import fr.estecka.variantscit.format.IStringTransform;
import fr.estecka.variantscit.format.NbtAdapter;
import fr.estecka.variantscit.format.properties.IStringProperty;
import fr.estecka.variantscit.format.properties.ItemComponentProperty;
import fr.estecka.variantscit.format.properties.TransformableProperty;
import fr.estecka.variantscit.format.transforms.SuccessiveTransform;

public class ComponentDataModule<P extends IStringProperty>
implements ISimpleCitModule
{
	static public final MapCodec<ComponentDataModule<IStringProperty>> CODEC = IStringProperty.MAP_CODEC.xmap(ComponentDataModule::new, o->o.property);

	@Deprecated
	static public final <T> MapCodec<ComponentDataModule<TransformableProperty<ItemComponentProperty<T>>>> CreateLegacyCodec(DataComponentType<T> componentType){
		return CodecUtil.WithWarning(
			LegacyPropertyCodec(componentType).xmap(ComponentDataModule::new, o->o.property),
			"Module types `custom_data`, `entity_data`, `bucket_entity_data` and `block_entity_data` are deprecated. Use `component_data` instead."
		);
	}

	@Deprecated
	static public final <T> MapCodec<TransformableProperty<ItemComponentProperty<T>>> LegacyPropertyCodec(DataComponentType<T> componentType){
		return RecordCodecBuilder.mapCodec(builder->builder
			.group(
				CodecUtil.MapWithAlternative(NbtAdapter.MAPCODEC, NbtAdapter.LEGACY_MAPCODEC).forGetter(o->o.inner().nbtAdapter),
				CodecUtil.MapWithAlternative(SuccessiveTransform.CODEC.fieldOf("transform"), IStringTransform.LEGACY_CODEC.fieldOf("lowercase")).orElse(IStringTransform.NOOP).forGetter(o->o.transform())
			)
			.apply(builder, (adapter, transform) -> new TransformableProperty<>(new ItemComponentProperty<>(componentType, adapter), transform, Optional.empty()))
		);
	}

	private final P property;

	public ComponentDataModule(P property){
		this.property = property;
	}

	@Override
	public CacheKeySet GetCacheKeys() {
		return CacheKeySet.Of(property.GetCacheKey());
	}

	@Override
	public ECachePolicy GetCachePolicy() {
		return ECachePolicy.ALWAYS;
	}

	@Override
	public @Nullable ResourceLocation GetItemVariant(ItemStack stack) {
		String result = this.property.GetPropertyString(stack);
		return (result!=null) ? ResourceLocation.tryParse(result) : null;
	}

	@Override
	public @Nullable ResourceLocation Walkthrough(ItemStack stack, IVariantLibrary library, CommandLogger logger) {
		String raw = TransformableProperty.GetRaw(this.property).GetPropertyString(stack);
		String transformed = property.GetPropertyString(stack);

		logger.Info("Raw data: {}",    CommandLogger.ItemData(raw, "Missing or invalid"));
		logger.Info("Transformed: {}", CommandLogger.ItemData(transformed));

		return this.GetItemModel(stack, library);
	}
}
