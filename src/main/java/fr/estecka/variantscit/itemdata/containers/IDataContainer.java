package fr.estecka.variantscit.itemdata.containers;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

public interface IDataContainer
{
	@NotNull Object value();

	default @Nullable String asString(){
		return this.value() instanceof String string ? string :
		       this.value() instanceof StringTag nbt ? nbt.getAsString() :
		       this.value() instanceof Number number ? number.toString() :
		       this.value() instanceof NumericTag nbt ? nbt.getAsNumber().toString() :
		       this.value() instanceof ResourceLocation id ? id.toString() :
		       null
		       ;
	};

	default @Nullable Number asNumber(){
		return this.value() instanceof Number number ? number :
		       this.value() instanceof NumericTag nbt ? nbt.getAsNumber() :
		       null
		       ;
	};

	default @Nullable Tag asNbt(){
		return this.value() instanceof Tag nbt ? nbt : null;
	};
}
