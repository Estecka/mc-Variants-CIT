package fr.estecka.variantscit.modules;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.estecka.variantscit.api.ISimpleCitModule;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Identifier;

public class CustomDataModule
implements ISimpleCitModule
{
	static public final MapCodec<CustomDataModule> CODEC = RecordCodecBuilder.mapCodec(builder->builder
		.group(
			Codec.STRING.fieldOf("nbtKey").forGetter(s->s.key),
			Codec.BOOL.fieldOf("caseSensitive").orElse(true).forGetter(s->s.caseSensitive)
		)
		.apply(builder, CustomDataModule::new)
	);

	private final String key;
	private final boolean caseSensitive;

	public CustomDataModule(String key, boolean caseSensitive){
		this.key = key;
		this.caseSensitive = caseSensitive;
	}

	@Override
	public Identifier GetItemVariant(ItemStack stack){
		NbtComponent component = stack.get(DataComponentTypes.CUSTOM_DATA);
		NbtCompound nbt;

		if (component==null || (nbt=component.getNbt())==null || !nbt.contains(key, NbtElement.STRING_TYPE))
			return null;

		String rawVariant = nbt.getString(key);
		if (!caseSensitive)
			rawVariant = rawVariant.toLowerCase();

		return Identifier.tryParse(rawVariant);
	}
}
