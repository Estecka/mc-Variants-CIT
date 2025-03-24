package fr.estecka.variantscit.modules;

import java.util.HashMap;
import java.util.Map;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.estecka.variantscit.VariantsCitMod;
import fr.estecka.variantscit.format.Substitution;
import fr.estecka.variantscit.format.properties.IStringProperty;
import fr.estecka.variantscit.format.properties.ItemComponentProperty;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;

public class MultiComponentFormatModule
extends ASimpleMultiComponentCachingModule
{
	static public final MapCodec<MultiComponentFormatModule> CODEC = RecordCodecBuilder.mapCodec(builder->builder
		.group(
			Codec.BOOL.fieldOf("debug").orElse(false).forGetter(mod -> mod.debug),
			Substitution.CODEC.fieldOf("format").forGetter(m->m.format),
			Codecs.strictUnboundedMap(Substitution.VARNAME_CODEC, Codec.withAlternative(IStringProperty.REGISTRY.codec, ItemComponentProperty.MAP_CODEC.codec())).fieldOf("variables").forGetter(m->m.varGetters)
		)
		.apply(builder, MultiComponentFormatModule::new)
	);

	private final Substitution format;
	private final Map<String, IStringProperty> varGetters;

	public MultiComponentFormatModule(boolean debug, Substitution format, Map<String,IStringProperty> variables){
		super(debug, variables.values().stream());
		this.format = format;
		this.varGetters = Map.copyOf(variables);

		this.format.MatchWarning(variables.keySet());
	}

	@Override
	public Identifier RecomputeItemVariant(ItemStack stack){
		Map<String,String> variables = new HashMap<>();

		for (var entry : this.varGetters.entrySet()){
			String value = entry.getValue().GetPropertyString(stack);
			if (value == null)
				return null;

			variables.put(entry.getKey(), value);
		}

		String rawId = this.format.Substitute(variables);
		variables.clear();
		Identifier id = Identifier.tryParse(rawId);
		if (debug)
			VariantsCitMod.LOGGER.info("component_format: \"{}\" -> \"{}\"", this.format, rawId);
		return id;
	}
}
