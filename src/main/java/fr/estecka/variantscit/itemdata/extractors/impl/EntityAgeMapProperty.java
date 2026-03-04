package fr.estecka.variantscit.itemdata.extractors.impl;

import org.jetbrains.annotations.NotNull;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.component.CustomData;

public class EntityAgeMapProperty
extends AMonoComponentProperty<CustomData,String>
{
	static public final EntityAgeMapProperty UNIT = new EntityAgeMapProperty("", "_baby");

	static public final MapCodec<EntityAgeMapProperty> MAP_CODEC = RecordCodecBuilder.mapCodec(builder->
		builder.group(
			Codec.STRING.optionalFieldOf("adult", UNIT.adult).forGetter(o->o.adult),
			Codec.STRING.optionalFieldOf("baby", UNIT.baby).forGetter(o->o.baby)
		)
		.apply(builder, EntityAgeMapProperty::new)
	);

	private final String adult, baby;

	public EntityAgeMapProperty(String adult, String baby){
		super(DataComponents.BUCKET_ENTITY_DATA);
		this.adult = adult;
		this.baby = baby;
	}

	@Override
	public @NotNull String GetPropertyValue(CustomData bucket) {
		CompoundTag nbt;

		if (bucket == null || (nbt=bucket.getUnsafe()) == null || !nbt.contains("Age", Tag.TAG_ANY_NUMERIC))
			return adult;

		float age = nbt.getFloat("Age");
		return (age>=0) ? adult : baby;
	}
}
