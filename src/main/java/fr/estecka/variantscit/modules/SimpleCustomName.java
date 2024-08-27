package fr.estecka.variantscit.modules;

import java.util.Map;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.estecka.variantscit.api.ISimpleCitModule;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class SimpleCustomName
implements ISimpleCitModule
{
	static public final MapCodec<Map<String,Identifier>> CODEC = RecordCodecBuilder.mapCodec(builder->builder
		.group(
			Codec.unboundedMap(Codec.STRING, Identifier.CODEC).fieldOf("specialNames").orElse(Map.of()).forGetter(map->map)
		)
		.apply(builder, map->map)
	);

	private final Map<String,Identifier> specialNames;

	public SimpleCustomName(Map<String,Identifier> specialNames){
		this.specialNames = specialNames;
	}

	@Override
	public Identifier GetItemVariant(ItemStack stack){
		Text component = stack.get(DataComponentTypes.CUSTOM_NAME);
		if (component == null)
			return null;

		String name = component.getString();
		Identifier variant = specialNames.get(name);
		return (variant != null) ? variant : Identifier.tryParse(name);
	}
}
