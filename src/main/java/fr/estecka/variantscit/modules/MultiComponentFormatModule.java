package fr.estecka.variantscit.modules;

import java.util.HashMap;
import java.util.Map;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.estecka.variantscit.CodecUtil;
import fr.estecka.variantscit.VariantsCitMod;
import fr.estecka.variantscit.format.Substitution;
import fr.estecka.variantscit.format.properties.ItemComponentProperty;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;

public class MultiComponentFormatModule
extends ASimpleMultiComponentCachingModule
{


	static public final MapCodec<MultiComponentFormatModule> CODEC = RecordCodecBuilder.mapCodec(builder->builder
		.group(
			Codec.BOOL.fieldOf("debug").orElse(false).forGetter(mod -> mod.debug),
			Substitution.CODEC.fieldOf("format").forGetter(m->m.format),
			Codecs.strictUnboundedMap(Substitution.VARNAME_CODEC, ItemComponentProperty.CODEC).fieldOf("variables").forGetter(m->m.varGetters)
		)
		.apply(builder, MultiComponentFormatModule::new)
	);

	private final Substitution format;
	private final Map<String, ItemComponentProperty> varGetters;

	public MultiComponentFormatModule(boolean debug, Substitution format, Map<String, ItemComponentProperty> variables){
		super(debug, variables.values().stream().map(ItemComponentProperty::componentType));
		this.format = format;
		this.varGetters = Map.copyOf(variables);

		this.format.MatchWarning(variables.keySet());
	}

	@Override
	public Identifier RecomputeItemVariant(ItemStack stack){
		Map<String,String> variables = new HashMap<>();

		for (var entry : this.varGetters.entrySet()){
			NbtElement nbt = CodecUtil.GetComponentNbt(stack, entry.getValue().componentType());
			if (nbt == null)
				return null;

			String value = entry.getValue().nbtAdapter().ResolveData(nbt);
			if (value == null)
				return null;

			variables.put(entry.getKey(), value);
		}

		String rawId = this.format.Substitute(variables);
		variables.clear();
		Identifier id = Identifier.tryParse(rawId);
		if (debug)
				VariantsCitMod.LOGGER.info("multi_component_format: \"{}\" -> \"{}\"", this.format, rawId);
		return id;
	}
}
