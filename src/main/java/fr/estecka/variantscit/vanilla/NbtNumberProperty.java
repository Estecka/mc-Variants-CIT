package fr.estecka.variantscit.vanilla;

import org.jetbrains.annotations.Nullable;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;


public class NbtNumberProperty
implements RangeSelectItemModelProperty
{
	static public final MapCodec<NbtNumberProperty> CreateCodec(DataComponentType<CustomData> componentType){
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
	private final DataComponentType<CustomData> dataType;

	private NbtNumberProperty(DataComponentType<CustomData> dataType, String path) {
		this.dataType = dataType;
		this.path = ParsePath(path);
	}

	@Override
	public MapCodec<NbtNumberProperty> type(){
		return CreateCodec(dataType);
	}

	@Override
	public float get(ItemStack stack, @Nullable ClientLevel world, @Nullable LivingEntity entity, int seed){
		return GetValueForComponent(stack.get(dataType));
	}


	public float GetValueForComponent(CustomData component){
		Tag nbt;
		if (component==null || (nbt=component.getUnsafe())==null)
			return 0;

		for (int i=0; i<path.length; ++i)
		if  (nbt instanceof CompoundTag compound)
			nbt = compound.get(path[i]);
		else
			return 0;

		if (nbt instanceof NumericTag num)
			return num.getAsFloat();
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
