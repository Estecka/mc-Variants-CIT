package fr.estecka.variantscit.util;

import java.util.Collection;

public class CollectionUtil
{
	static public String ConcatToString(Collection<?> collection){
		StringBuilder builder = new StringBuilder();
		boolean first = true;

		for (Object object : collection) {
			if (!first)
				builder.append(", ");
			builder.append(object.toString());
			first = false;
		}

		return builder.toString();
	}
}
