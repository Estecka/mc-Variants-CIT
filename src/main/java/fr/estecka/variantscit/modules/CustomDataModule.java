package fr.estecka.variantscit.modules;

import java.util.Optional;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.estecka.variantscit.VariantsCitMod;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtString;
import net.minecraft.util.Identifier;

public class CustomDataModule
extends ASimpleComponentCachingModule<NbtComponent>
{
	static public final MapCodec<CustomDataModule> CODEC = RecordCodecBuilder.mapCodec(builder->builder
		.group(
			Codec.STRING.optionalFieldOf("nbtKey").deprecated(0).validate(_0 -> {
				VariantsCitMod.LOGGER.warn("The custom_data parameter `nbtKey` is being deprecated. Use `nbtPath` instead.");
				return DataResult.success(_0);
			}).forGetter(s->Optional.empty()),
			Codec.STRING.optionalFieldOf("nbtPath").forGetter(s->Optional.empty()),
			Codec.BOOL.fieldOf("caseSensitive").orElse(true).forGetter(s->s.caseSensitive)
		)
		.apply(builder, CustomDataModule::new)
	);

	/**
	 * TODO: implement proper getter for the codec.
	 */
	private final String[] path;
	private final boolean caseSensitive;

	private CustomDataModule(Optional<String> key, Optional<String> path, boolean caseSensitive)
	throws IllegalStateException
	{
		super(DataComponentTypes.CUSTOM_DATA);
		this.caseSensitive = caseSensitive;
		if (path.isPresent())
			this.path = ParsePath(path.get());
		else if (key.isPresent())
			this.path = new String[]{ key.get() };
		else
			throw new IllegalStateException("Nbt path not set");
	}

	@Override
	public Identifier GetVariantForComponent(NbtComponent component){
		NbtElement nbt;
		if (component==null || (nbt=component.getNbt())==null)
			return null;

		for (int i=0; i<path.length; ++i)
		if  (nbt instanceof NbtCompound compound)
			nbt = compound.get(path[i]);
		else
			return null;

		if (!(nbt instanceof NbtString))
			return null;

		String rawVariant = nbt.asString();
		if (!caseSensitive)
			rawVariant = rawVariant.toLowerCase();

		return Identifier.tryParse(rawVariant);
	}

	static private String[] ParsePath(String rawPath)
	throws IllegalStateException
	{
		String[] result = rawPath.split("\\.");

		for (String s : result)
			if (s.isEmpty())
				throw new IllegalStateException("Malformatted path: "+rawPath);

		return result;
	}
}
