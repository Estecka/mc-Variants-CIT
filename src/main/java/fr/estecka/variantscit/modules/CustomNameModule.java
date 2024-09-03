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
			Codec.BOOL.fieldOf("keepIllegal").orElse(false).forGetter(p->p.keepIllegal),
			Codec.unboundedMap(Codec.STRING, Identifier.CODEC).fieldOf("specialNames").orElse(Map.of()).forGetter(p->p.specialNames)
		)
		.apply(builder, CustomNameModule::new)
	);

	/*
	 * Using  a Text (i.e, the item's component) instead of  a string  key means
	 * that the lifetime of the each entry is roughly equivalent to the lifetime
	 * of the associated item stack.
	 * Keys  are  evaluated  by identity, not  by content. Item  components  are
	 * supposed to be immutable, so the value of text should change.
	 */
	private final WeakHashMap<Text, Identifier> cachedVariants = new WeakHashMap<>();

	private final boolean caseSensitive;
	private final boolean keepIllegal;
	private final Map<String,Identifier> specialNames = new HashMap<>();

	public CustomNameModule(boolean caseSensitive, boolean keepIllegal, Map<String, Identifier> specialNames){
		this.caseSensitive = caseSensitive;
		this.keepIllegal = keepIllegal;

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

		if (!keepIllegal){
			name = name.replace(' ', '_');
			name.replaceAll("[^a-zA-Z0-9_.-]", "");
		}

		VariantsCitMod.LOGGER.warn("Cached {}: {} -> {}", cachedVariants.size(), text.getString(), name);
		return Identifier.tryParse(name);
	}
}
