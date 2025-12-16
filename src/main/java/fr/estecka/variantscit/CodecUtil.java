package fr.estecka.variantscit;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.jetbrains.annotations.Nullable;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.ComponentType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

public final class CodecUtil
{
	static private final MinecraftClient client = MinecraftClient.getInstance();

	static public final Codec<Identifier> VCIT_IDENTIFIER = Codec.STRING.comapFlatMap(CodecUtil::VCitIdenfitier, Identifier::toString);
	static public final Codec<String> IDENTIFIER_PATH = Codec.STRING.validate(path->Identifier.isPathValid(path) ? DataResult.success(path) : DataResult.error(()->"Invalid character in path: "+path));
	static public final Codec<String> IDENTIFIER_NAMESPACE = Codec.STRING.validate(path->Identifier.isNamespaceValid(path) ? DataResult.success(path) : DataResult.error(()->"Invalid character in namespace: "+path));
	static public final Codec<String> NONEMPTY_STRING = Codec.STRING.validate(string->string.isEmpty() ? DataResult.error(()->"String cannot be empty") : DataResult.success(string));
	static public final Codec<Character> CHAR = Codec.string(1,1).xmap(s->s.charAt(0), c->String.valueOf(c));
	static public final Codec<Pattern> REGEX = Codec.STRING.comapFlatMap(CodecUtil::ParseRegex, Pattern::toString);

	/**
	 * Functions to be used in `validate()` on deprecated codecs.
	 */
	static public <T> Function<T,DataResult<T>> WithWarning(String warning, Object... args){
		return o->{
			VariantsCitMod.LOGGER.warn(warning, args);
			return DataResult.success(o);
		};
	}

	static public <T> MapCodec<T> WithWarning(MapCodec<T> codec, String warning, Object... args){
		return codec.validate(WithWarning(warning, args));
	}

	static public DataResult<Identifier> VCitIdenfitier(String input){
		if (!input.contains(":"))
			input = VariantsCitMod.MODID + ":" + input;
		return Identifier.validate(input);
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
			codec.fieldOf(alias).validate(WithWarning("VCIT field `{}` is deprecated. Use `{}` instead.", alias, primary))
		);
	}

	static public <K,V> Codec<V> Enum(Codec<K> keyCodec, Map<K,V> units){
		return keyCodec.<V>flatXmap(
			key -> units.containsKey(key) ?
				DataResult.success(units.get(key)) :
				DataResult.error(()->"Unknown key: " + key.toString()),
			obj -> units.containsValue(obj) ?
				DataResult.success(units.entrySet().stream().filter(entry->obj.equals(entry.getValue())).map(Entry::getKey).findFirst().get()) :
				DataResult.error(()->"Unknown unit")
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

	static public DataResult<Pattern> ParseRegex(String regex){
		try {
			return DataResult.success(Pattern.compile(regex));
		}
		catch (PatternSyntaxException e){
			return DataResult.error(e::toString);
		}
	}

	/**
	 * @param directory The base directory, *without* the trailing `/`
	 * @param suffix The file extension, including the deparating dot.
	 */
	static public Map<Identifier,Resource> GetResources(ResourceManager manager, String directory, String suffix){
		return manager.findResources(directory, id->id.getPath().endsWith(suffix));
	}

	static public Identifier AssetIdFromResourceId(Identifier id, String directory, String suffix){
		return id.withPath(path->path.substring(
			directory.length() + 1,
			path.length() - suffix.length()
		));
	}

	static public <T>  DataResult<T> ParseResource(Resource resource, MapCodec<T> codec){
		return ParseResource(resource, codec.codec());
	}

	static public <T>  DataResult<T> ParseResource(Resource resource, Codec<T> codec){
		JsonElement json;
		try {
			json = JsonParser.parseReader(resource.getReader());
		}
		catch (IOException|JsonParseException e){
			return DataResult.error(e::toString);
		}

		return codec.decode(JsonOps.INSTANCE, json).map(Pair::getFirst);
	}

	static public <T> Map<Identifier, T> ReloadResources(ResourceManager manager, Codec<T> codec, String directory, String suffix){
		Map<Identifier, T> results = new HashMap<>();

		Map<Identifier, Resource> resources = GetResources(manager, directory, suffix);
		for (var entry : resources.entrySet()){
			Identifier id = CodecUtil.AssetIdFromResourceId(entry.getKey(), directory, suffix);
			DataResult<T> result = ParseResource(entry.getValue(), codec);
			if (result.isSuccess())
				results.put(id, result.getOrThrow());
			else {
				VariantsCitMod.LOGGER.error("Error loading resource {}:\n{}",
					entry.getKey(),
					result.error().get().message()
				);
			}
		}

		return results;
	}
}
