package fr.estecka.variantscit.itemdata.containers;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import fr.estecka.variantscit.itemdata.transforms.DataConversions;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.MutableComponent;

public interface IDataContainer
{
	@NotNull Object value();
	@NotNull MutableComponent toText();

	default @Nullable String asString(){
		return DataConversions.AggressiveString(this);
	};

	default @Nullable Number asNumber(){
		return DataConversions.AggressiveNumber(this);
	};

	default @Nullable Tag asNbt(){
		return DataConversions.SoftCastToNbt(this.value());
	};

	static public String NullableAsString(IDataContainer container){
		return (container == null) ? null : container.asString();
	}

	static public String printableValue(IDataContainer data){
		String result = data.asString();
		if (result == null)
			result = data.value().toString();
		return result;
	}
}
