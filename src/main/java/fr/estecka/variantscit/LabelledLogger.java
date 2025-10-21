package fr.estecka.variantscit;

import java.util.EmptyStackException;
import java.util.Stack;
import java.util.function.BiConsumer;
import org.slf4j.LoggerFactory;

public class LabelledLogger
{
	public final org.slf4j.Logger logger = LoggerFactory.getLogger(VariantsCitMod.MODID);
	private final Stack<Object> labels = new Stack<>();

	public org.slf4j.Logger Unlabelled(){
		return this.logger;
	}

	public void PushLabel(Object tag){
		this.labels.push(tag);
	}

	public void PopLabel(){
		try {
			labels.pop();
		}
		catch(EmptyStackException e){
			logger.error("Logger stack underflow.");
		}
	}

	public void Reset(){
		labels.clear();
	}

	public void Print(BiConsumer<String,Object[]> printer, String format, Object... args){
		StringBuilder builder = new StringBuilder();
		for (Object tag : this.labels){
			builder.append('[');
			builder.append(tag.toString());
			builder.append(']');

		}

		builder.append(" ");
		builder.append(format);
		printer.accept(builder.toString(), args);
	}

	public void info (String format, Object... args){ Print(logger::info,  format, args); }
	public void warn (String format, Object... args){ Print(logger::warn,  format, args); }
	public void error(String format, Object... args){ Print(logger::error, format, args); }
	public void trace(String format, Object... args){ Print(logger::trace, format, args); }

}
