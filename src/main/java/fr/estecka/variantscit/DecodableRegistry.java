package fr.estecka.variantscit;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapDecoder;
import net.minecraft.util.Identifier;

public class DecodableRegistry<T>
{
	public interface IDecoWrapper<T> extends Function<MapCodec<? extends T>, MapDecoder<? extends T>> {}

	private final Map<Identifier, Decoder<? extends T>> entries = new HashMap<>();

	private final String typeKey;
	private final IDecoWrapper<T> valueWrapper;

	public final Decoder<Identifier> typeCodec;
	public final Codec<T> codec;

	public DecodableRegistry(String typeKey){
		this(typeKey, c->c);
	}

	public DecodableRegistry(String typeKey, IDecoWrapper<T> valueWrapper){
		this.typeKey = typeKey;
		this.valueWrapper = valueWrapper;

		this.typeCodec = Codec.withAlternative(
			Identifier.CODEC,
			Identifier.CODEC.fieldOf(this.typeKey).codec()
		);

		this.codec = Codec.of(
			Encoder.error("Encoding not supported"),
			this::Decode
		);
	}

	public void Register(Identifier key, T unit){
		this.Register(key, MapCodec.unit(unit), unit);
	}

	public void Register(Identifier key, MapCodec<? extends T> mapCodec){
		entries.put(key, valueWrapper.apply(mapCodec).decoder());
	}

	public <U extends T> void Register(Identifier key, MapCodec<U> mapCodec, U unit){
		entries.put(key, Codec.withAlternative(
			Codec.<T>of(Encoder.error("Encoding not supported"), valueWrapper.apply(mapCodec).decoder().map(o->o)),
			IdToUnitCodec(key, unit)
		));
	}

	/**
	 * Basically a unit codec, but will apply to plain identifiers, not to misconfigured maps.
	 * The vanilla unit codec on the other hand, will fail to apply to primitive types.
	 */
	static public <T> Codec<T> IdToUnitCodec(Identifier key, T unit){
		return Identifier.CODEC.xmap(k->unit, u->key);
	}

	public Decoder<? extends T> GetDecoder(Identifier type){
		return this.entries.get(type);
	}

	public <I> DataResult<Pair<T,I>> Decode(DynamicOps<I> ops, I data){
		var typeResult = this.typeCodec.decode(ops, data);
		if (!typeResult.isSuccess())
			return typeResult.map(_0->null);

		Decoder<? extends T> codec = this.entries.get(typeResult.getOrThrow().getFirst());
		return codec.decode(ops, data).map(o->o.mapFirst(u->u));
	}

	public <I> DataResult<Pair<T,I>> Decode(Dynamic<I> data){
		return this.Decode(data.getOps(), data.getValue());
	}

}
