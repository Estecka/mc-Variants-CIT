package fr.estecka.variantscit.format.properties;

import org.jetbrains.annotations.NotNull;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

public class EntityAgeMapProperty
extends AMonoComponentProperty<NbtComponent>
{
	private final String adult, baby;

	public EntityAgeMapProperty(String adult, String baby){
		super(DataComponentTypes.BUCKET_ENTITY_DATA);
		this.adult = adult;
		this.baby = baby;
	}

	@Override
	public @NotNull String GetPropertyString(NbtComponent bucket) {
		NbtCompound nbt;

		if (bucket == null || (nbt=bucket.getNbt()) == null || !nbt.contains("Age", NbtElement.NUMBER_TYPE))
			return adult;

		float age = nbt.getFloat("Age");
		return (age>=0) ? adult : baby;
	}
}
