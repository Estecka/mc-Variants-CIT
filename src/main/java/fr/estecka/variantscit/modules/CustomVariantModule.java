package fr.estecka.variantscit.modules;

import java.util.Map;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.estecka.variantscit.api.ISimpleCitModule;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Identifier;

public class CustomVariantModule
implements ISimpleCitModule
{
	static public final MapCodec<String> CODEC = RecordCodecBuilder.mapCodec(builder->builder
		.group(
			Codec.STRING.fieldOf("nbtKey").forGetter(s->s)
		)
		.apply(builder, s->s)
	);

	private final String key;

	public CustomVariantModule(Map<String, ModelIdentifier> specialModels, String key){
		this.key = key;
	}

	@Override
	public Identifier GetItemVariant(ItemStack stack){
		NbtComponent component = stack.get(DataComponentTypes.CUSTOM_DATA);
		NbtCompound nbt;

		if (component==null || (nbt=component.getNbt())==null || !nbt.contains(key, NbtElement.STRING_TYPE))
			return null;

		return Identifier.tryParse(nbt.getString(key));
	}
}
