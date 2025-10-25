package fr.estecka.variantscit;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;
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
import net.minecraft.util.Identifier;

public class DecodableRegistry<T>
{
	public interface IMapWrapper<T> extends Function<MapCodec<? extends T>, MapDecoder<? extends T>> {}

	private final Map<Identifier, T> units = new HashMap<>();
	private final Map<Identifier, MapDecoder<? extends T>> mapCodecs = new HashMap<>();

	private final String typeKey;
	private final IMapWrapper<T> mapWrapper;

	private final MapDecoder<Identifier> typeCodec;
	public final Codec<T>    unitCodec = CodecUtil.Enum(Identifier.CODEC, this.units);
	public final MapCodec<T> mapCodec  = MapCodec.of(new MapEncoderImpl(), new MapDecoderImpl());
	public final Codec<T>    codec     = Codec.withAlternative(this.unitCodec, this.mapCodec.codec());

	public DecodableRegistry(String typeKey){
		this(typeKey, null, c->c);
	}

	public DecodableRegistry(String typeKey, Identifier defaultId){
		this(typeKey, defaultId, c->c);
	}

	public DecodableRegistry(String typeKey, IMapWrapper<T> mapWrapper){
		this(typeKey, null, mapWrapper);
	}

	public DecodableRegistry(String typeKey, @Nullable Identifier defaultId, IMapWrapper<T> mapWrapper){
		this.typeKey = typeKey;
		this.mapWrapper = mapWrapper;
		if (defaultId == null)
			this.typeCodec = Identifier.CODEC.fieldOf(this.typeKey);
		else
			this.typeCodec = Identifier.CODEC.optionalFieldOf(this.typeKey, defaultId);
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

	private class MapDecoderImpl
	extends CompressorHolder
	implements MapDecoder<T>
	{
		@Override
		public <I> DataResult<T> decode(DynamicOps<I> ops, MapLike<I> data){
			var typeResult = typeCodec.decode(ops, data);
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
