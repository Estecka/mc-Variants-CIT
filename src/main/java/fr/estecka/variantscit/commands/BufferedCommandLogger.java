package fr.estecka.variantscit.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;

public class BufferedCommandLogger
extends CommandLogger
{
	private static record LogEntry(
		BiConsumer<CommandLogger,Component> printer,
		Component message
	) {}

	private final List<LogEntry> queue = new ArrayList<>();

	public BufferedCommandLogger(CommandContext<FabricClientCommandSource> commandContext){
		super(commandContext);
	}

	@Override
	public void Info(Component message) {
		queue.add(new LogEntry(CommandLogger::Info, labels.AddLabels(message)));
	}

	@Override
	public void Error(Component message) {
		queue.add(new LogEntry(CommandLogger::Error, labels.AddLabels(message)));
	}

	public boolean IsEmpty(){
		return queue.isEmpty();
	}

	public void Flush(CommandLogger logger){
		for (var entry : this.queue)
			entry.printer.accept(logger, entry.message);

		queue.clear();
	}
}
