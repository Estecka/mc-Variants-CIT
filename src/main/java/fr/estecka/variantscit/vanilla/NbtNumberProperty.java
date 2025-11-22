package fr.estecka.variantscit.vanilla;

import org.jetbrains.annotations.Nullable;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.render.item.property.numeric.NumericProperty;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.component.ComponentType;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.AbstractNbtNumber;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;


public class NbtNumberProperty
implements NumericProperty
{
	static public final MapCodec<NbtNumberProperty> CreateCodec(ComponentType<NbtComponent> componentType){
		return RecordCodecBuilder.mapCodec(builder->builder
			.group(
				Codec.STRING.fieldOf("nbtPath").forGetter(s->"")
			)
			.apply(builder, (a)->new NbtNumberProperty(componentType, a))
		);
	}

	/**
	 * TODO: implement proper getter for the codec.
	 */
	private final String[] path;
	private final ComponentType<NbtComponent> dataType;

	private NbtNumberProperty(ComponentType<NbtComponent> dataType, String path) {
		this.dataType = dataType;
		this.path = ParsePath(path);
	}

	@Override
	public MapCodec<NbtNumberProperty> getCodec(){
		return CreateCodec(dataType);
	}

	@Override
	public float getValue(ItemStack stack, @Nullable ClientWorld world, @Nullable LivingEntity entity, int seed){
		return GetValueForComponent(stack.get(dataType));
	}


	public float GetValueForComponent(NbtComponent component){
		NbtElement nbt;
		if (component==null || (nbt=component.getNbt())==null)
			return 0;

		for (int i=0; i<path.length; ++i)
		if  (nbt instanceof NbtCompound compound)
			nbt = compound.get(path[i]);
		else
			return 0;

		if (nbt instanceof AbstractNbtNumber num)
			return num.floatValue();
		else
			return 0;
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
