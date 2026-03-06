package fr.estecka.variantscit.itemdata.extractors.impl;

import java.util.Optional;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.estecka.variantscit.CodecUtil;
import fr.estecka.variantscit.itemdata.containers.ComponentContainer;
import fr.estecka.variantscit.itemdata.containers.IDataContainer;
import fr.estecka.variantscit.itemdata.extractors.IDataExtractor;
import fr.estecka.variantscit.itemdata.extractors.TransformableExtractor;
import fr.estecka.variantscit.itemdata.transforms.DataConversions;
import fr.estecka.variantscit.itemdata.transforms.IDataTransform;
import fr.estecka.variantscit.itemdata.transforms.IStringTransform;
import fr.estecka.variantscit.itemdata.transforms.impl.NbtPath;
import fr.estecka.variantscit.modules.cache.ComponentCacheKey;
import fr.estecka.variantscit.modules.cache.ICacheKey;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;


public record ItemComponentProperty<T>(
	DataComponentType<T> componentType,
	Optional<NbtPath> nbtPath,
	IDataTransform expectedType
)
implements IDataExtractor
{
	@SuppressWarnings("unchecked")
	static public final MapCodec<ItemComponentProperty<?>> MAP_CODEC = RecordCodecBuilder.mapCodec(builder->builder
		.group(
			BuiltInRegistries.DATA_COMPONENT_TYPE.byNameCodec().fieldOf("componentType").forGetter(o->o.componentType),
			NbtPath.CODEC.optionalFieldOf("nbtPath").forGetter(adp -> adp.nbtPath),
			DataConversions.EXPECT_GROUP_CODEC.optionalFieldOf("expect", IDataTransform.NOOP).forGetter(adp -> adp.expectedType)
		)
		.apply(builder, ItemComponentProperty::new)
	);

	static public final Codec<ItemComponentProperty<?>> MONOSTRING_DECODER = Codec.STRING.flatXmap(
		ItemComponentProperty::MonostringParse,
		CodecUtil::NoEncode
	);

	static public final Codec<TransformableExtractor<ItemComponentProperty<?>>> SANE_MONOSTRING_DECODER = MONOSTRING_DECODER.xmap(
		inner -> new TransformableExtractor<>(inner, IStringTransform.SANITIZE_AUTO, Optional.empty()),
		TransformableExtractor::inner
	);

	@Override
	public ICacheKey GetCacheKey() {
		return new ComponentCacheKey<>(componentType);
	}

	@Override
	public IDataContainer Extract(ItemStack stack) {
		T component = stack.get(componentType);
		if (component == null)
			return null;

		IDataContainer result = new ComponentContainer<T>(component, componentType);
		if (this.nbtPath.isPresent())
			result = nbtPath.get().LooseTypedTransform(result);

		return expectedType.LooseTypedTransform(result);
	}

	static private DataResult<ItemComponentProperty<?>> MonostringParse(String input){
		int pathLocation;
		for (pathLocation = 0; pathLocation<input.length(); pathLocation++){
			char c = input.charAt(pathLocation);
			if (c == '.' || !ResourceLocation.isAllowedInResourceLocation(c))
				break;
		}

		String componentName = input.substring(0, pathLocation);
		String pathString      = input.substring(pathLocation);

		var optPath = (!pathString.isEmpty()) ? NbtPath.Parse(pathString).map(Optional::of) : DataResult.<Optional<NbtPath>>success(Optional.empty());
		var optComponent = BuiltInRegistries.DATA_COMPONENT_TYPE.byNameCodec().decode(NbtOps.INSTANCE, StringTag.valueOf(componentName)).map(Pair::getFirst);

		if (optPath.isError())
			return optPath.map(__->null);

		if (optComponent.isError())
			return optComponent.map(__->null);

		Optional<NbtPath> path = optPath.getOrThrow();
		DataComponentType<?> component = optComponent.getOrThrow();

		return DataResult.success(new ItemComponentProperty<>(component, path, IDataTransform.NOOP));
	}
}
