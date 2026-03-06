package fr.estecka.variantscit.itemdata.containers;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import fr.estecka.variantscit.itemdata.transforms.DataConversions;
import net.minecraft.nbt.Tag;

public interface IDataContainer
{
	@NotNull Object value();

	static public String asString(IDataContainer container){
		return (container == null) ? null : container.asString();
	}

	default @Nullable String asString(){
		return DataConversions.AggressiveString(this);
	};

	default @Nullable Number asNumber(){
		return DataConversions.AggressiveNumber(this);
	};

	default @Nullable Tag asNbt(){
		return DataConversions.SoftCastToNbt(this.value());
	};
}
