package fr.estecka.variantscit.itemdata.containers;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import fr.estecka.variantscit.itemdata.transforms.DataConversions;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;

public interface IDataContainer
{
	@NotNull Object value();
	// Used for logging, not data conversions. Like 'Object::toString'
	@NotNull MutableComponent toText();


/******************************************************************************/
/* # Data Conversions                                                         */
/******************************************************************************/

	default @Nullable String asString(){
		return DataConversions.AggressiveString(this);
	};

	default @Nullable Identifier asIdentifier(){
		if (value() instanceof Identifier id)
			return id;
		else if (this.asString() instanceof String string)
			return Identifier.tryParse(string);
		else
			return null;
	};

	default @Nullable Number asNumber(){
		return DataConversions.AggressiveNumber(this);
	};

	default @Nullable Double asDouble(){
		if (this.asNumber() instanceof Number num)
			return num.doubleValue();
		else
			return null;
	};

	default @Nullable Tag asNbt(){
		return DataConversions.SoftCastToNbt(this.value());
	};


/******************************************************************************/
/* # Logging                                                                  */
/******************************************************************************/

	// TODO: make containers work more like Optionals/DataResult
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
