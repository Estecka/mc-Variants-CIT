package fr.estecka.variantscit.util.logging;

import java.util.EmptyStackException;
import java.util.Stack;
import fr.estecka.variantscit.VariantsCitMod;
import net.minecraft.network.chat.Component;

public class LogLabelStack
extends Stack<Object>
{
	private final Stack<Object> labels = new Stack<>();

	public Object pop(){
		try {
			return super.pop();
		}
		catch(EmptyStackException e){
			VariantsCitMod.LOGGER.error("{}", e);
			return null;
		}
	}

	public StringBuilder LabelString(){
		StringBuilder builder = new StringBuilder();
		for (Object tag : this.labels){
			builder.append('[');
			builder.append(tag.toString());
			builder.append(']');

		}

		if (!this.isEmpty())
			builder.append(" ");

		return builder;
	}

	public String AddLabels(String format){
		if (this.isEmpty())
			return format;
		else
			return LabelString().append(format).toString();
	}

	public Component AddLabels(Component text){
		if (this.isEmpty())
			return text;
		return Component.literal(LabelString().toString())
			.append(text)
			.withStyle(text.getStyle())
			;
	}
}
