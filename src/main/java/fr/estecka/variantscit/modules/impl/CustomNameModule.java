package fr.estecka.variantscit.modules.impl;

import java.text.Normalizer;
import java.util.Map;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

public class CustomNameModule
extends ASimpleMonoComponentModule<Component>
{
	static public final MapCodec<CustomNameModule> CODEC =
		Codec.unboundedMap(Codec.STRING, ResourceLocation.CODEC)
			.optionalFieldOf("specialNames", Map.of())
			.xmap(CustomNameModule::new, p->p.specialNames)
		;

	private final Map<String,ResourceLocation> specialNames;

	public CustomNameModule(Map<String, ResourceLocation> specialNames){
		super(DataComponents.CUSTOM_NAME);
		this.specialNames = specialNames;
	}

	@Override
	public ResourceLocation GetVariantForComponent(Component text){
		String name = text.getString();
		if (specialNames.containsKey(name))
			return specialNames.get(name);
		
		name = this.Transform(name);
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
