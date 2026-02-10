package fr.estecka.variantscit.modules.impl;

import java.text.Normalizer;
import java.util.Map;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.estecka.variantscit.VariantsCitMod;

public class CustomNameModule
extends ASimpleComponentCachingModule<Component>
{
	static public final MapCodec<CustomNameModule> CODEC = RecordCodecBuilder.mapCodec(builder->builder
		.group(
			Codec.BOOL.fieldOf("debug").orElse(false).forGetter(p->p.debug),
			Codec.unboundedMap(Codec.STRING, ResourceLocation.CODEC).optionalFieldOf("specialNames", Map.of()).forGetter(p->p.specialNames)
		)
		.apply(builder, CustomNameModule::new)
	);

	private final boolean debug;
	private final Map<String,ResourceLocation> specialNames;

	public CustomNameModule(boolean debug, Map<String, ResourceLocation> specialNames){
		super(DataComponents.CUSTOM_NAME);
		this.debug = debug;
		this.specialNames = specialNames;
	}

	@Override
	public ResourceLocation GetVariantForComponent(Component text){
		String name = text.getString();
		if (specialNames.containsKey(name))
			return specialNames.get(name);
		
		name = this.Transform(name);
		if (debug)
			VariantsCitMod.LOGGER.info("[custom_name VCIT] #{} \"{}\" -> `{}`", super.cachedVariants.size(), text.getString(), name);
		return ResourceLocation.tryParse(name);
	}

	public String Transform(String name){
		return Normalizer.normalize(name, Normalizer.Form.NFD)
			.replace(' ', '_')
			.toLowerCase()
			.replaceAll("[^a-zA-Z0-9_.-]", "")
			;
	}
}
