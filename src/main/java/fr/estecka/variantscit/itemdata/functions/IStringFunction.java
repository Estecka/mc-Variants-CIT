package fr.estecka.variantscit.itemdata.functions;

import java.util.function.Function;
import fr.estecka.variantscit.itemdata.containers.IDataContainer;
import fr.estecka.variantscit.itemdata.containers.RawDataContainer;

@FunctionalInterface
public interface IStringFunction
extends IDataFunction, Function<String,String>
{
	@Override
	default IDataContainer LooseTypedTransform(IDataContainer container) {
		String input = container.asString();
		if (input == null)
			return null;
		String output = this.apply(input);
		if (output == null)
			return null;

		return RawDataContainer.OfNullable(output);
	}
	
}
