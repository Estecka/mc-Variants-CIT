package fr.estecka.variantscit.util.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsoleLogger
{
	public final Logger logger;
	public final LogLabelStack labels = new LogLabelStack();

	public ConsoleLogger(String name){
		this.logger = LoggerFactory.getLogger(name);
	}

	public Logger Unlabelled(){
		return this.logger;
	}

	public void info (String format, Object... args){ logger.info (labels.AddLabels(format), args); }
	public void warn (String format, Object... args){ logger.warn (labels.AddLabels(format), args); }
	public void error(String format, Object... args){ logger.error(labels.AddLabels(format), args); }
	public void trace(String format, Object... args){ logger.trace(labels.AddLabels(format), args); }

}
