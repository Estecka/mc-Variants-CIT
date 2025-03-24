package fr.estecka.variantscit;

import java.util.HashMap;
import java.util.Map;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
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


	public void Register(Identifier key, T value){
		entries.put(key, Codec.unit(value));
	}

	public void Register(Identifier key, Codec<? extends T> value){
		if (valueKey != null)
			value = value.fieldOf(valueKey).codec();
		entries.put(key, value);
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
