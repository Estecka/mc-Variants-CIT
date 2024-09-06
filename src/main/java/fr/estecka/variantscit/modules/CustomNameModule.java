package fr.estecka.variantscit.modules;

import java.text.Normalizer;
import java.util.Map;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.estecka.variantscit.VariantsCitMod;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class CustomNameModule
extends ASimpleComponentCachingModule<Text>
{
	static public final MapCodec<CustomNameModule> CODEC = RecordCodecBuilder.mapCodec(builder->builder
		.group(
			Codec.BOOL.fieldOf("debug").orElse(false).forGetter(p->p.debug),
			Codec.unboundedMap(Codec.STRING, Identifier.CODEC).fieldOf("specialNames").orElse(Map.of()).forGetter(p->p.specialNames)
		)
		.apply(builder, CustomNameModule::new)
	);

	private final boolean debug;
	private final Map<String,Identifier> specialNames;

	public CustomNameModule(boolean debug, Map<String, Identifier> specialNames){
		super(DataComponentTypes.CUSTOM_NAME);
		this.debug = debug;
		this.specialNames = specialNames;
	}

	@Override
	public Identifier GetVariantForComponent(Text text){
		String name = text.getString();
		if (specialNames.containsKey(name))
			return specialNames.get(name);
		
		name = this.Transform(name);
		if (debug)
			VariantsCitMod.LOGGER.info("[custom_name CIT] #{} \"{}\" -> `{}`", super.cachedVariants.size(), text.getString(), name);
		return Identifier.tryParse(name);
	}

	public String Transform(String name){
		return Normalizer.normalize(name, Normalizer.Form.NFD)
			.replace(' ', '_')
			.toLowerCase()
			.replaceAll("[^a-zA-Z0-9_.-]", "")
			;
	}
}
