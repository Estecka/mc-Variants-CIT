package fr.estecka.variantscit;

import java.util.HashMap;
import java.util.Map;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.MapCodec;
import net.minecraft.util.Identifier;

public class DecodableRegistry<T>
{
	private final Map<Identifier, Codec<? extends T>> entries = new HashMap<>();

	private final String typeKey;
	private final String valueKey;

	public final Codec<Identifier> typeCodec;
	public final Codec<T> codec;

	public DecodableRegistry(String typeKey){
		this(typeKey, null);
	}

	public DecodableRegistry(String typeKey, String valueKey){
		this.typeKey = typeKey;
		this.valueKey = valueKey;

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
		entries.put(key, Codec.withAlternative(Codec.unit(unit), IdToUnitCodc(key, unit)));
	}

	public void Register(Identifier key, MapCodec<? extends T> mapCodec){
		if (valueKey != null)
			mapCodec = mapCodec.fieldOf(valueKey);
		entries.put(key, mapCodec.codec());
	}

	public <U extends T> void Register(Identifier key, MapCodec<U> mapCodec, U unit){
		if (valueKey != null)
			mapCodec = mapCodec.fieldOf(valueKey);
		
		Codec<U> codec = Codec.withAlternative(
			mapCodec.codec(),
			IdToUnitCodc(key, unit)
		);

		entries.put(key, codec);
	}

	/**
	 * Basically a unit codec, but will apply to plain identifiers, not to misconfigured maps.
	 * The vanilla unit codec on the other hand, will fail to apply to primitive types.
	 */
	static public <T> Codec<T> IdToUnitCodc(Identifier key, T unit){
		return Identifier.CODEC.xmap(k->unit, u->key);
	}

	public Codec<? extends T> GetCodec(Identifier type){
		return this.entries.get(type);
	}

	public <I> DataResult<Pair<T,I>> Decode(DynamicOps<I> ops, I data){
		var typeResult = this.typeCodec.decode(ops, data);
		if (!typeResult.isSuccess())
			return typeResult.map(_0->null);

		Codec<? extends T> codec = this.entries.get(typeResult.getOrThrow().getFirst());
		return codec.decode(ops, data).map(o->o.mapFirst(u->u));
	}

	public <I> DataResult<Pair<T,I>> Decode(Dynamic<I> data){
		return this.Decode(data.getOps(), data.getValue());
	}

}
