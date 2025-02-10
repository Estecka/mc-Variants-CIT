package fr.estecka.variantscit.modules;

import java.util.Optional;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.estecka.variantscit.NbtPath;
import fr.estecka.variantscit.VariantsCitMod;
import net.minecraft.component.ComponentType;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.nbt.AbstractNbtNumber;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtString;
import net.minecraft.util.Identifier;

public class NbtStringModule
extends ASimpleComponentCachingModule<NbtComponent>
{
	static public final MapCodec<NbtStringModule> CreateCodec(ComponentType<NbtComponent> componentType){
			return RecordCodecBuilder.mapCodec(builder->builder
				.group(
					Codec.STRING.optionalFieldOf("nbtKey").deprecated(0).validate(_0 -> {
						VariantsCitMod.LOGGER.warn("The custom_data parameter `nbtKey` is being deprecated. Use `nbtPath` instead.");
						return DataResult.success(_0);
					}).forGetter(s->Optional.empty()),
					NbtPath.CODEC.optionalFieldOf("nbtPath").forGetter(s->Optional.of(s.path)),
					Codec.BOOL.fieldOf("caseSensitive").orElse(true).forGetter(s->s.caseSensitive)
				)
				.apply(builder, (a,b,c)->new NbtStringModule(componentType, a, b, c))
			);
		}

	private final String[] path;
	private final boolean caseSensitive;

	private NbtStringModule(ComponentType<NbtComponent> dataType, Optional<String> key, Optional<String[]> path, boolean caseSensitive)
	throws IllegalStateException
	{
		super(dataType);
		this.caseSensitive = caseSensitive;
		if (path.isPresent())
			this.path = path.get();
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

		if (!(nbt instanceof NbtString) && !(nbt instanceof AbstractNbtNumber))
			return null;

		String rawVariant = nbt.asString();
		if (!caseSensitive)
			rawVariant = rawVariant.toLowerCase();

		return Identifier.tryParse(rawVariant);
	}
}
