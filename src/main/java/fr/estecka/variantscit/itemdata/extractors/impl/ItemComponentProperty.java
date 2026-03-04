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
import fr.estecka.variantscit.itemdata.transforms.IDataConversions;
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
	Optional<IDataConversions<?>> dataType
)
implements IDataExtractor
{
	static public final MapCodec<ItemComponentProperty<?>> MAP_CODEC = RecordCodecBuilder.mapCodec(builder->builder
		.group(
			BuiltInRegistries.DATA_COMPONENT_TYPE.byNameCodec().fieldOf("componentType").forGetter(o->o.componentType),
			NbtPath.CODEC.optionalFieldOf("nbtPath").forGetter(adp -> adp.nbtPath),
			IDataConversions.LEGACY_GROUP_CODEC.optionalFieldOf("expect").forGetter(adp -> adp.dataType)
		)
		.apply(builder, ItemComponentProperty::new)
	);

	static public final Codec<TransformableExtractor<ItemComponentProperty<?>>> MONOSTRING_DECODER = Codec.STRING.flatXmap(
		ItemComponentProperty::MonostringParse,
		CodecUtil::NoEncode
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
		if (dataType.isPresent())
			result = dataType.get().LooseTypedTransform(result);
		return result;
	}

	static private DataResult<TransformableExtractor<ItemComponentProperty<?>>> MonostringParse(String input){
		int pathLocation;
		for (pathLocation = 0; pathLocation<input.length(); pathLocation++){
			char c = input.charAt(pathLocation);
			if (c == '.' || !ResourceLocation.isAllowedInResourceLocation(c))
				break;
		}

		String s_component = input.substring(0, pathLocation);
		String s_path      = input.substring(pathLocation);

		var r_path = (!s_path.isEmpty()) ? NbtPath.Parse(s_path) : DataResult.success(NbtPath.IDENTITY);
		var r_component = BuiltInRegistries.DATA_COMPONENT_TYPE.byNameCodec().decode(NbtOps.INSTANCE, StringTag.valueOf(s_component)).map(Pair::getFirst);

		if (r_path.isError())
			return r_path.map(__->null);

		if (r_component.isError())
			return r_component.map(__->null);

		NbtPath path = r_path.getOrThrow();
		DataComponentType<?> component = r_component.getOrThrow();

		return DataResult.success(new TransformableExtractor<>(
			new ItemComponentProperty<>(component, Optional.of(path), Optional.empty()),
			IStringTransform.SANITIZE_AUTO, // FIXME Can't use that in preconditions
			Optional.empty()
		));
	}
}
