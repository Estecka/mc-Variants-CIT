package fr.estecka.variantscit;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.Nullable;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.ItemStack;

public final class CodecUtil
{
	static private final Minecraft client = Minecraft.getInstance();

	static public final Codec<ResourceLocation> VCIT_IDENTIFIER = Codec.STRING.comapFlatMap(CodecUtil::VCitIdentifier, ResourceLocation::toString);
	static public final Codec<String> IDENTIFIER_PATH = Codec.STRING.validate(path->ResourceLocation.isValidPath(path) ? DataResult.success(path) : DataResult.error(()->"Invalid character in path: "+path));
	static public final Codec<String> LEGACY_ITEM_PATH = IDENTIFIER_PATH.validate(CodecUtil::UnItemify);
	static public final Codec<String> IDENTIFIER_NAMESPACE = Codec.STRING.validate(path->ResourceLocation.isValidNamespace(path) ? DataResult.success(path) : DataResult.error(()->"Invalid character in namespace: "+path));
	@Deprecated
	static public final Codec<String> NONEMPTY_STRING = Codec.STRING.validate(CodecUtil.NonEmptyString("<unspecified> string"));
	static public final Codec<Character> CHAR = Codec.string(1,1).xmap(s->s.charAt(0), c->String.valueOf(c));
	static public final Codec<Pattern> REGEX = Codec.STRING.comapFlatMap(CodecUtil::ParseRegex, Pattern::toString);
	static public final Codec<Tag> NBT_ELEMENT = Codec.PASSTHROUGH.xmap( dyn->dyn.convert(NbtOps.INSTANCE).getValue(), nbt->new Dynamic<>(NbtOps.INSTANCE, nbt.copy()) );


/******************************************************************************/
/* # Base Types                                                               */
/******************************************************************************/

	static public DataResult<ResourceLocation> NamespacedIdentifier(String defaultNamespace, String input){
		if (!input.contains(":"))
			input = defaultNamespace + ":" + input;
		return ResourceLocation.read(input);
	}

	static public DataResult<ResourceLocation> VCitIdentifier(String input){
		return NamespacedIdentifier(VariantsCitMod.MODID, input);
	}

	static public DataResult<Pattern> ParseRegex(String regex){
		try {
			return DataResult.success(Pattern.compile(regex));
		}
		catch (PatternSyntaxException e){
			return DataResult.error(e::toString);
		}
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

	static public String UnItemifyRaw(String modelPrefix){
		if (modelPrefix.startsWith("item/")){
			modelPrefix = modelPrefix.substring("item/".length());
		}
		return modelPrefix;
	}

	static public DataResult<String> UnItemify(String original){
		return DataResult.success(UnItemifyRaw(original));
	}

	static public DataResult<ResourceLocation> UnItemify(ResourceLocation original){
		return DataResult.success(ResourceLocation.fromNamespaceAndPath(original.getNamespace(), UnItemifyRaw(original.getPath())));
	}

	static public DataResult<String> NonEmptyString(String string, String stringName){
		return string.isEmpty() ?
			DataResult.error(()->stringName+" cannot be empty.") :
			DataResult.success(string);
	}

	static public Function<String,DataResult<String>> NonEmptyString(String stringName){
		return string -> NonEmptyString(string, stringName);
	}


/******************************************************************************/
/* # Codec Modifiers                                                          */
/******************************************************************************/

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

	static public <T> Codec<T> WithWarning(Codec<T> codec, String warning, Object... args){
		return codec.validate(WithWarning(warning, args));
	}

	static public <I,O> DataResult<O> NoEncode(I _0){
		return DataResult.error(()->"Encoding not supported.");
	}

	@Deprecated
	static public <I,O> Function<I,O> NoGetter(){
		return (I _0) -> { throw new NotImplementedException("Encoding not supported due to missing getter."); };
	}

	static public <I,O> Function<I,O> NoGetter(String getterName){
		return (I _0) -> { throw new NotImplementedException("Encoding not supported due to missing getter: "+getterName); };
	}

	static public <T> Codec<List<T>> OneOrMany(Codec<T> original){
		var listCodec = original.listOf();
		return WithAlternative(
			listCodec,
			Codec.of(listCodec, original.map(List::of))
		);
	}

	static public <T> Codec<T> WithAlternative(Codec<T> primary, Codec<? extends T> alternative){
		assert primary != null && alternative != null;
		return Codec.withAlternative(
			Objects.requireNonNull(primary),
			Objects.requireNonNull(alternative)
		);
	}

	static public <T> MapCodec<T> MapWithAlternative(MapCodec<? extends T> primary, MapCodec<? extends T> alternative){
		return MapCodec.assumeMapUnsafe(
			WithAlternative(
				Anonymize(primary.codec()),
				alternative.codec()
			)
		);
	}

	@SafeVarargs
	static public <T> Codec<T> WithAlternatives(Codec<? extends T> primary, Codec<? extends T>... altArray){
		Codec<T> result = Anonymize(primary);

		for (var alt : altArray)
			result = WithAlternative(result, alt);

		return result;
	}

	@SafeVarargs
	static public <T> MapCodec<T> MapWithAlternatives(MapCodec<? extends T> primaryMap, MapCodec<? extends T>... mapArray){
		@SuppressWarnings("unchecked")
		Codec<? extends T>[] codecArray = new Codec[mapArray.length];
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

	static public <T> MapCodec<Optional<T>> OptionalWithAlias(Codec<T> codec, String primary, String alias){
		return WithAlias(codec, primary, alias)
			.xmap(Optional::of, Optional::get)
			.orElse(Optional.empty())
			;
	}

	/**
	 * Downcast the decoder's result to a superclass. This strips the codec of
	 * its encoding abilities.
	 */
	static public <SUPER, SUB extends SUPER> MapCodec<SUPER> AnonymizeMap(MapCodec<SUB> original){
		return original.flatXmap(
			o->DataResult.success((SUPER)o),
			CodecUtil::NoEncode
		);
	}
	static public <SUPER, SUB extends SUPER> Codec<SUPER> Anonymize(Codec<SUB> original){
		return original.flatXmap(
			o->DataResult.success((SUPER)o),
			CodecUtil::NoEncode
		);
	}


/******************************************************************************/
/* # Decoding/Encoding                                                        */
/******************************************************************************/

	static public <T> DataResult<T> ParseString(Codec<T> codec, String string){
		return codec.parse(NbtOps.INSTANCE, StringTag.valueOf(string));
	}

	static public String ShortIdString(ResourceLocation id){
		if (id.getNamespace() == ResourceLocation.DEFAULT_NAMESPACE)
			return id.getPath();
		else
			return id.toString();
	}

	static public String ShortIdString(DataComponentType<?> componentType){
		String result = "???";
		ResourceLocation id = BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(componentType);
		if (id != null)
			result = CodecUtil.ShortIdString(id);
		return result;
	}

	static public <T> @Nullable Tag GetComponentNbt(ItemStack stack, DataComponentType<T> type){
		T component = stack.get(type);
		if (component == null)
			return null;
		else
			return GetComponentNbt(component, type.codecOrThrow());
	}

	static public <T> @Nullable Tag GetComponentNbt(T component, Codec<T> codec){
		DynamicOps<Tag> nbtOps = NbtOps.INSTANCE;
		// Enables encoding of data from dynamic registries
		if (client.level != null)
			nbtOps = client.level.registryAccess().createSerializationContext(nbtOps);

		var dataResult = codec.encodeStart(nbtOps, component);
		if (dataResult.isSuccess())
			return dataResult.getOrThrow();
		else {
			VariantsCitMod.LOGGER.error("Unable to serialize component: {}", dataResult.error().get().message() );
			return null;
		}
	}


/******************************************************************************/
/* # Resource Reload                                                          */
/******************************************************************************/

	/**
	 * @param directory	The base directory, *without* the trailing `/`
	 * @param suffix	The file extension, including the separating dot.
	 * @return The matching  resources, keyed using  an ID that includes neither
	 * the directory nor the suffix.
	 */
	static public Map<ResourceLocation,Resource> GetResources(ResourceManager manager, String directory, String suffix){
		Map<ResourceLocation, Resource> result = new HashMap<>();
		var resources = manager.listResources(directory, id->id.getPath().endsWith(suffix));
		for (var e : resources.entrySet())
			result.put(AssetIdFromResourceId(e.getKey(), directory, suffix), e.getValue());

		return result;
	}

	static public ResourceLocation AssetIdFromResourceId(ResourceLocation id, String directory, String suffix){
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
			json = JsonParser.parseReader(resource.openAsReader());
		}
		catch (IOException|JsonParseException e){
			return DataResult.error(e::toString);
		}

		return codec.decode(JsonOps.INSTANCE, json).map(Pair::getFirst);
	}

	static public <T> Map<ResourceLocation, T> ReloadResources(ResourceManager manager, Codec<T> codec, String directory, String suffix){
		Map<ResourceLocation, T> results = new HashMap<>();

		Map<ResourceLocation, Resource> resources = GetResources(manager, directory, suffix);
		for (var entry : resources.entrySet()){
			ResourceLocation id = entry.getKey();
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
