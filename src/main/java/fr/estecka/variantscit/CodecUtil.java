package fr.estecka.variantscit;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import org.jetbrains.annotations.Nullable;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.ComponentType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.Identifier;

public class CodecUtil
{
	static private final MinecraftClient client = MinecraftClient.getInstance();

	static public final Codec<String> IDENTIFIER_PATH = Codec.STRING.validate(path->Identifier.isPathValid(path) ? DataResult.success(path) : DataResult.error(()->"Invalid character in path: "+path));
	static public final Codec<String> IDENTIFIER_NAMESPACE = Codec.STRING.validate(path->Identifier.isNamespaceValid(path) ? DataResult.success(path) : DataResult.error(()->"Invalid character in namespace: "+path));

	/**
	 * Functions to be used in `validate()` on deprecated codecs.
	 */
	static public <T> Function<T,DataResult<T>> WithWarning(String warning, Object... args){
		return o->{
			VariantsCitMod.LOGGER.warn(warning, args);
			return DataResult.success(o);
		};
	}

	static public <T> Codec<List<T>> OneOrMany(Codec<T> original){
		var listCodec = original.listOf();
		return Codec.withAlternative(
			listCodec,
			Codec.of(listCodec, original.map(List::of))
		);
	}

	static public <T> MapCodec<T> MapWithAlternative(MapCodec<T> primary, MapCodec<? extends T> alternative){
		return MapCodec.assumeMapUnsafe(
			Codec.withAlternative(
				primary.codec(),
				alternative.codec()
			)
		);
	}

	@SafeVarargs
	static public <T> Codec<T> WithAlternatives(Codec<T> primary, Codec<T>... altArray){
		int i = altArray.length - 1;
		Codec<T> alternative = altArray[i];

		for (i=i-1; i>=0; --i){
			alternative = Codec.withAlternative(altArray[i], alternative);
		}

		return Codec.withAlternative(primary, alternative);
	}

	@SafeVarargs
	static public <T> MapCodec<T> MapWithAlternatives(MapCodec<T> primaryMap, MapCodec<T>... mapArray){
		@SuppressWarnings("unchecked")
		Codec<T>[] codecArray = new Codec[mapArray.length];
		for (int i=0; i<mapArray.length; ++i)
			codecArray[i] = mapArray[i].codec();
		return MapCodec.assumeMapUnsafe(WithAlternatives(primaryMap.codec(), codecArray));
	}

	static public <T> MapCodec<T> WithAlias(Codec<T> codec, String primary, String alias){
		return MapWithAlternative(
				codec.fieldOf(primary),
				codec.fieldOf(alias).validate(WithWarning("VCIT field `{}` is deprecated. Use `context` instead.", alias))
			);
	}

	static public <T> MapCodec<Optional<T>> OptionalWithAlias(Codec<T> codec, String primary, String alias){
		return WithAlias(codec, primary, alias)
			.xmap(Optional::of, Optional::get)
			.orElse(Optional.empty())
			;
	}

	static public <T> @Nullable NbtElement GetComponentNbt(ItemStack stack, ComponentType<T> type){
		T component = stack.get(type);
		if (component == null)
			return null;
		else
			return GetComponentNbt(component, type.getCodecOrThrow());
	}

	static public <T> @Nullable NbtElement GetComponentNbt(T component, Codec<T> codec){
		DynamicOps<NbtElement> nbtOps = NbtOps.INSTANCE;
		// Enables encoding of data from dynamic registries
		if (client.world != null)
			nbtOps = client.world.getRegistryManager().getOps(nbtOps);

		var dataResult = codec.encodeStart(nbtOps, component);
		if (dataResult.isSuccess())
			return dataResult.getOrThrow();
		else {
			VariantsCitMod.LOGGER.error("Unable to serialize component: {}", dataResult.error().get().message() );
			return null;
		}
	}

	/**
	 * Downcast the decoder's result to a superclass. This strips the codec of
	 * its encoding abilities.
	 */
	static public <SUPER, SUB extends SUPER> MapCodec<SUPER> Anonymize(MapCodec<SUB> original){
		return original.flatXmap(
			o->DataResult.success((SUPER)o),
			o->DataResult.error(()->"Encoding not supported by anonymized codec.")
		);
	}
}
