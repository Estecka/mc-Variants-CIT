package fr.estecka.variantscit.modules;

import java.util.HashMap;
import java.util.Map;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.estecka.variantscit.VariantsCitMod;
import fr.estecka.variantscit.format.NbtAdapter;
import fr.estecka.variantscit.format.Substitution;
import net.minecraft.component.ComponentType;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;

@Deprecated
public class SingleComponentFormatModule<T>
extends AArbitraryComponentModule<T>
{
	static public final MapCodec<SingleComponentFormatModule<?>> CODEC = RecordCodecBuilder.mapCodec(builder->builder
		.group(
			Registries.DATA_COMPONENT_TYPE.getCodec().fieldOf("componentType").forGetter(m->m.componentType),
			Codec.BOOL.fieldOf("debug").orElse(false).forGetter(mod -> mod.debug),
			Substitution.CODEC.fieldOf("format").forGetter(m->m.format),
			Codecs.strictUnboundedMap(Substitution.VARNAME_CODEC, NbtAdapter.CODEC).fieldOf("variables").forGetter(m->m.varGetters)
		)
		.apply(builder, SingleComponentFormatModule::new)
	);

	private final Substitution format;
	private final Map<String, NbtAdapter> varGetters;

	public SingleComponentFormatModule(ComponentType<T> type, boolean debug, Substitution format, Map<String, NbtAdapter> variables){
		super(type, debug);
		this.format = format;
		this.varGetters = Map.copyOf(variables);

		this.format.MatchWarning(variables.keySet());
	}

	public Identifier GetVariantForNbt(NbtElement nbt){
		Map<String,String> values = new HashMap<>();

		for (var entry : this.varGetters.entrySet()){
			String data = entry.getValue().ResolveData(nbt);
			if (data == null)
				return null;
			values.put(entry.getKey(), data);
		}

		String rawId = this.format.Substitute(values);
		Identifier id = Identifier.tryParse(rawId);
		if (id == null)
				VariantsCitMod.LOGGER.warn("Substitution resulted in an invalid identifier: \"{}\" -> \"{}\"", this.format, rawId);
		return id;
	}

}
