package fr.estecka.variantscit.format.properties;

import org.jetbrains.annotations.NotNull;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.nbt.NbtCompound;

public class EntityAgeMapProperty
extends AMonoComponentProperty<NbtComponent>
{
	static public final EntityAgeMapProperty UNIT = new EntityAgeMapProperty("", "_baby");

	static public final MapCodec<EntityAgeMapProperty> MAP_CODEC = RecordCodecBuilder.mapCodec(builder->
		builder.group(
			Codec.STRING.fieldOf("adult").orElse(UNIT.adult).forGetter(o->o.adult),
			Codec.STRING.fieldOf("baby").orElse(UNIT.baby).forGetter(o->o.baby)
		)
		.apply(builder, EntityAgeMapProperty::new)
	);

	private final String adult, baby;

	public EntityAgeMapProperty(String adult, String baby){
		super(DataComponentTypes.BUCKET_ENTITY_DATA);
		this.adult = adult;
		this.baby = baby;
	}

	@Override
	public @NotNull String GetPropertyString(NbtComponent bucket) {
		NbtCompound nbt;

		if (bucket == null || (nbt=bucket.getNbt()) == null)
			return adult;

		float age = nbt.getFloat("Age", 0);
		return (age>=0) ? adult : baby;
	}
}
