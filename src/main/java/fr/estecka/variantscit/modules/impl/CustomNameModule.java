package fr.estecka.variantscit.modules.impl;

import java.util.Map;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import fr.estecka.variantscit.itemdata.transforms.IStringTransform;
import fr.estecka.variantscit.modules.cache.ECachePolicy;

public class CustomNameModule
extends ASimpleMonoComponentModule<Component>
{
	static public final MapCodec<CustomNameModule> MAPCODEC =
		Codec.unboundedMap(Codec.STRING, Identifier.CODEC)
			.optionalFieldOf("specialNames", Map.of())
			.xmap(CustomNameModule::new, p->p.specialNames)
		;

	private final Map<String,Identifier> specialNames;

	public CustomNameModule(Map<String, Identifier> specialNames){
		super(DataComponents.CUSTOM_NAME, ECachePolicy.ALWAYS);
		this.specialNames = specialNames;
	}

	@Override
	public Identifier GetVariantForComponent(Component text){
		String name = text.getString();
		if (specialNames.containsKey(name))
			return specialNames.get(name);
		
		name = IStringTransform.SANITIZE.apply(name);
		return Identifier.tryParse(name);
	}
}
