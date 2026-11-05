package fr.estecka.variantscit.itemdata.transforms.impl;

import java.util.Optional;
import java.util.function.Supplier;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import fr.estecka.variantscit.commands.CommandLogger;
import fr.estecka.variantscit.itemdata.containers.IDataContainer;
import fr.estecka.variantscit.itemdata.transforms.IDataTransform;
import net.minecraft.ChatFormatting;

public record LogTransform(Optional<String> label)
implements IDataTransform
{
	static public final MapCodec<LogTransform> MAPCODEC = Codec.STRING
		.optionalFieldOf("label")
		.xmap(LogTransform::new, LogTransform::label)
		;

	static public final MapCodec<LogTransform> ANONYMOUS_MAPCODEC = Codec.STRING
		.fieldOf("label")
		.xmap(Optional::of, Optional::get)
		.xmap(LogTransform::new, LogTransform::label)
		;

	static private CommandLogger LOGGER = null;

	public LogTransform(){
		this(Optional.empty());
	}

	static public void Log(Optional<String> label, IDataContainer data){
		if (LOGGER == null)
			; // No-op
		else if (label.isPresent()){
			LOGGER.Info(ChatFormatting.GRAY,
				"[Transform] {}: {}",
				label.get(),
				CommandLogger.ItemData(data)
			);
		}
		else {
			LOGGER.Info(
				ChatFormatting.GRAY,
				"[Transform] {}",
				CommandLogger.ItemData(data)
			);
		}
	}

	@Override
	public IDataContainer LooseTypedTransform(IDataContainer input) {
		Log(this.label, input);
		return input;
	}

	// TODO: There's probably a cleaner way to do it.
	static public <T> T WithLogger(CommandLogger logger, Supplier<T> supplier){
		var oldLogger = LOGGER;
		LOGGER = logger;
		T r;
		try {
			r = supplier.get();
		}
		finally {
			LOGGER = oldLogger;
		}
		return r;
	}
}
