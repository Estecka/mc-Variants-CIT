package fr.estecka.variantscit.modules;

import java.util.HashMap;
import java.util.Map;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.estecka.variantscit.api.ISimpleCitModule;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class CustomNameModule
implements ISimpleCitModule
{
	static public final MapCodec<CustomNameModule> CODEC = RecordCodecBuilder.mapCodec(builder->builder
		.group(
			Codec.BOOL.fieldOf("caseSensitive").orElse(false).forGetter(p->p.caseSensitive),
			Codec.unboundedMap(Codec.STRING, Identifier.CODEC).fieldOf("specialNames").orElse(Map.of()).forGetter(p->p.specialNames)
		)
		.apply(builder, CustomNameModule::new)
	);

	private final boolean caseSensitive;
	private final Map<String,Identifier> specialNames = new HashMap<>();

	public CustomNameModule(Boolean caseSensitive, Map<String, Identifier> specialNames){
		this.caseSensitive = caseSensitive;
		if (caseSensitive)
			this.specialNames.putAll(specialNames);
		else for (var e : specialNames.entrySet())
			this.specialNames.put(e.getKey().toLowerCase(), e.getValue());
	}

	@Override
	public Identifier GetItemVariant(ItemStack stack){
		Text component = stack.get(DataComponentTypes.CUSTOM_NAME);
		if (component == null)
			return null;

		String name = component.getString();
		if (!caseSensitive)
			name = name.toLowerCase();

		Identifier variant = specialNames.get(name);
		return (variant != null) ? variant : Identifier.tryParse(name);
	}
}
