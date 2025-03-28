package fr.estecka.variantscit.modules;

import java.util.stream.Stream;
import org.jetbrains.annotations.Nullable;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.estecka.variantscit.CodecUtil;
import fr.estecka.variantscit.format.EStringTransform;
import fr.estecka.variantscit.format.NbtAdapter;
import fr.estecka.variantscit.format.properties.IStringProperty;
import fr.estecka.variantscit.format.properties.ItemComponentProperty;
import fr.estecka.variantscit.format.properties.TransformableProperty;
import net.minecraft.component.ComponentType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class ComponentDataModule<P extends IStringProperty>
extends ASimpleMultiComponentCachingModule
{
	static public final MapCodec<ComponentDataModule<IStringProperty>> CODEC = RecordCodecBuilder.mapCodec(builder->builder
		.group(
			CodecUtil.MapWithAlternative(IStringProperty.REGISTRY.mapCodec, ItemComponentProperty.TRANSFORMABLE_CODEC).forGetter(o->o.property),
			Codec.BOOL.fieldOf("debug").orElse(false).forGetter(o->o.debug)
		)
		.apply(builder, ComponentDataModule::new)
	);

	@Deprecated
	static public final <T> MapCodec<ComponentDataModule<TransformableProperty<ItemComponentProperty>>> CreateLegacyCodec(ComponentType<T> componentType){
		return RecordCodecBuilder.mapCodec(builder->builder
			.group(
				LegacyPropertyCodec(componentType).forGetter(o->o.property),
				Codec.BOOL.fieldOf("debug").orElse(false).forGetter(o -> o.debug)
			)
			.apply(builder, (property, debug) -> new ComponentDataModule<>(property, debug))
		);
	}

	@Deprecated
	static public final <T> MapCodec<TransformableProperty<ItemComponentProperty>> LegacyPropertyCodec(ComponentType<T> componentType){
		return RecordCodecBuilder.mapCodec(builder->builder
			.group(
				CodecUtil.MapWithAlternative(NbtAdapter.MAPCODEC, NbtAdapter.LEGACY_MAPCODEC).forGetter(o->o.inner().nbtAdapter()),
				CodecUtil.MapWithAlternative(EStringTransform.ARRAY_CODEC.fieldOf("transform"), EStringTransform.LEGACY_CODEC.fieldOf("lowercase")).orElse(EStringTransform.EMPTY).forGetter(o->o.transform())
			)
			.apply(builder, (adapter, transform) -> new TransformableProperty<>(new ItemComponentProperty(componentType, adapter), transform))
		);
	}

	private final P property;

	public ComponentDataModule(P property, boolean debug){
		super(debug, Stream.of(property));
		this.property = property;
	}

	@Override
	public @Nullable Identifier RecomputeItemVariant(ItemStack stack) {
		String result = this.property.GetPropertyString(stack);
		return (result!=null) ? Identifier.tryParse(result) : null;
	}
}
