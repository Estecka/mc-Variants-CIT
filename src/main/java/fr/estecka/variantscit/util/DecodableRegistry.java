package fr.estecka.variantscit.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.mojang.serialization.Codec;
import com.mojang.serialization.CompressorHolder;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapDecoder;
import com.mojang.serialization.MapEncoder;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import fr.estecka.variantscit.VariantsCitMod;

public class DecodableRegistry<T>
{
	public interface IMapWrapper<T> extends Function<MapCodec<? extends T>, MapDecoder<? extends T>> {}

	private final Map<Identifier, T> units = new HashMap<>();
	private final Map<Identifier, MapDecoder<? extends T>> mapCodecs = new HashMap<>();

	private final String typeKey;
	private final IMapWrapper<T> mapWrapper;

	// private final Codec<Identifier> typeCodec;
	private final MapDecoder<Identifier> typeMapCodec;
	private final Map<Identifier,String> deprecationWarnings = new HashMap<>();

	public final MapCodec<T> mapCodec = MapCodec.of(new MapEncoderImpl(), new MapDecoderImpl());
	public final Codec<T>    unitCodec;
	public final Codec<T>    codec;

	static public class Builder<T> {
		private final @NotNull String keyname;
		private @NotNull Codec<Identifier> keyCodec = CodecUtil.VCIT_IDENTIFIER;
		private Identifier defaultKey = null;
		private IMapWrapper<T> wrapper = c->c;

		public Builder(String keyname){
			this.keyname = keyname;
		}

		public Builder<T> WithKeyCodec(Codec<Identifier> value){ this.keyCodec = value;   return this; }
		public Builder<T> WithWrapper (IMapWrapper<T> value)   { this.wrapper = value;    return this; }
		public Builder<T> WithDefault (Identifier value)       { this.defaultKey = value; return this; }

		public DecodableRegistry<T> Build(){
			return new DecodableRegistry<>(keyname, keyCodec, defaultKey, wrapper);
		}

	}

	public DecodableRegistry(String typeKey){
		this(typeKey, CodecUtil.VCIT_IDENTIFIER, null, c->c);
	}

	public DecodableRegistry(String typeKey, Codec<Identifier> typeCodec, @Nullable Identifier defaultId, IMapWrapper<T> mapWrapper){
		this.typeKey = typeKey;
		this.mapWrapper = mapWrapper;

		typeCodec = typeCodec.validate(this::Deprecated);
		if (defaultId == null)
			this.typeMapCodec = typeCodec.fieldOf(this.typeKey);
		else
			this.typeMapCodec = typeCodec.optionalFieldOf(this.typeKey, defaultId);

		this.unitCodec = CodecUtil.Enum(typeCodec, this.units);
		this.codec     = CodecUtil.WithAlternative(this.unitCodec, this.mapCodec.codec());
	}

	public void RegisterUnit(Identifier key, T unit){
		this.Register(key, MapCodec.unit(unit), unit);
	}

	public void RegisterMap(Identifier key, MapCodec<? extends T> mapCodec){
		AssertUnique(key);
		this.mapCodecs.put(key, mapWrapper.apply(mapCodec));
	}

	public <U extends T> void Register(Identifier key, MapCodec<U> mapCodec, U unit){
		AssertUnique(key);
		this.RegisterMap(key, mapCodec);
		this.units.put(key, unit);
	}

	public MapDecoder<? extends T> GetDecoder(Identifier type){
		return this.mapCodecs.get(type);
	}

	private void AssertUnique(Identifier key){
		if (this.units.containsKey(key) || this.mapCodecs.containsKey(key)){
			throw new IllegalStateException("Duplicate registration for entry:"+key.toString());
		}
	}

	public void Deprecate(Identifier id, String message){
		this.deprecationWarnings.put(id, message);
	}

	private <U> DataResult<U> Deprecated(U result){
		String warning = this.deprecationWarnings.get(result);
		if (warning != null)
			VariantsCitMod.LOGGER.warn("{}", warning);
		return DataResult.success(result);
	}

	private class MapDecoderImpl
	extends CompressorHolder
	implements MapDecoder<T>
	{
		@Override
		public <I> DataResult<T> decode(DynamicOps<I> ops, MapLike<I> data){
			var typeResult = typeMapCodec.decode(ops, data);
			if (!typeResult.isSuccess())
				return typeResult.map(_0->null);
	
			Identifier type = typeResult.getOrThrow();
			MapDecoder<? extends T> codec = mapCodecs.get(type);
			if (codec == null)
				return DataResult.error(()->"Unknown key: "+type);
			else
				return codec.decode(ops, data).map(o->o);
		}

		@Override
		public <K> Stream<K> keys(DynamicOps<K> ops) {
			Stream<K> result = Stream.of(ops.createString(typeKey));

			Iterator<Stream<K>> it = mapCodecs.values().stream().map(o->o.keys(ops)).iterator();
			while (it.hasNext()){
				result = Stream.concat(result, it.next());
			}

			return result;
		}
	}

	private class MapEncoderImpl
	extends CompressorHolder
	implements MapEncoder<T>
	{
		@Override
		public <O> RecordBuilder<O> encode(T input, DynamicOps<O> ops, RecordBuilder<O> prefix) {
			DataResult<MapLike<O>> result = DataResult.error(()->"Encoding not supported by DecodableRegistry");
			return result.map(map->prefix).result().orElse(prefix.withErrorsFrom(result));
		}

		@Override
		public <K> Stream<K> keys(DynamicOps<K> ops) {
			return Stream.of();
		}
	}
}
