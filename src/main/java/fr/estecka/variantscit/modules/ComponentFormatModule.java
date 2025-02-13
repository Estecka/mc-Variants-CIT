package fr.estecka.variantscit.modules;

import java.util.HashMap;
import java.util.Map;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.estecka.variantscit.VariantsCitMod;
import fr.estecka.variantscit.nbt.NbtAdapter;
import fr.estecka.variantscit.nbt.Substitution;
import net.minecraft.component.ComponentType;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;

public class ComponentFormatModule<T>
extends AArbitraryComponentModule<T>
{
	static public final MapCodec<ComponentFormatModule<?>> CODEC = RecordCodecBuilder.mapCodec(builder->builder
		.group(
			Registries.DATA_COMPONENT_TYPE.getCodec().fieldOf("componentType").forGetter(m->m.componentType),
			Substitution.CODEC.fieldOf("format").forGetter(m->m.format),
			Codecs.strictUnboundedMap(Substitution.VARNAME_CODEC, NbtAdapter.CODEC).fieldOf("variables").forGetter(m->m.varGetters)
		)
		.apply(builder, ComponentFormatModule::new)
	);

	private final Substitution format;
	private final Map<String, NbtAdapter> varGetters;

	public ComponentFormatModule(ComponentType<T> type, Substitution format, Map<String, NbtAdapter> variables){
		super(type);
		this.format = format;
		this.varGetters = Map.copyOf(variables);
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
			VariantsCitMod.LOGGER.warn("Substitution resulted in an invalid identifier: {} \"{}\"", this.componentType, this.format);
		return id;
	}

}
