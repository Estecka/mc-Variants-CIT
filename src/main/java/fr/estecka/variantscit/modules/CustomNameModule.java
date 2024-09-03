package fr.estecka.variantscit.modules;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.estecka.variantscit.VariantsCitMod;
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

	private final WeakHashMap<Text, Identifier> cachedVariants = new WeakHashMap<>();

	private final boolean caseSensitive;
	private final Map<String,Identifier> specialNames = new HashMap<>();
	private final Map<Character,Character> illegalConversions = new HashMap<>();
	{
		illegalConversions.put(' ', '_');
		illegalConversions.put('\'', '-');
	}

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

		return cachedVariants.computeIfAbsent(component, this::GetVariantFromText);
	}

	public Identifier GetVariantFromText(Text text){
		String name = text.getString();

		if (!caseSensitive)
			name = name.toLowerCase();

		if (specialNames.containsKey(name)){
			VariantsCitMod.LOGGER.warn("Cached {}: {} -> {}", cachedVariants.size(), text.getString(), name);
			return specialNames.get(name);
		}

		for (var e : illegalConversions.entrySet())
			name = name.replace(e.getKey(), e.getValue());

		VariantsCitMod.LOGGER.warn("Cached {}: {} -> {}", cachedVariants.size(), text.getString(), name);
		return Identifier.tryParse(name);
	}
}
