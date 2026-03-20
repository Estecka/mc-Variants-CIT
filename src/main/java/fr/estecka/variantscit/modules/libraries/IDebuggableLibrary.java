package fr.estecka.variantscit.modules.libraries;

import org.jetbrains.annotations.Nullable;
import fr.estecka.variantscit.commands.CommandLogger;
import net.minecraft.resources.ResourceLocation;

/**
 * Implementations MUST be of type LIB.
 */
public interface IDebuggableLibrary<LIB>
{
	public void Summary(CommandLogger logger);
	public void Dump(CommandLogger logger);
	public Snitch<LIB> CreateSnitch(CommandLogger logger);

	public default LIB GetLibrary(){
		// Unsafe
		return (LIB)this;
	}

	/**
	 * Implementations MUST be of type LIB.
	 */
	public abstract class Snitch<LIB> {
		protected final CommandLogger logger;
		private @Nullable ResourceLocation mainVariant = null;
		private boolean foundModel = false;

		protected Snitch(CommandLogger logger){
			this.logger = logger;
		}

		public LIB GetLibrary(){
			// Unsafe
			return (LIB)this;
		}

		protected void OnTriedVariant(ResourceLocation variant, boolean exists){
			this.foundModel |= exists;
			if (this.mainVariant == null)
				mainVariant = variant;
		}

		protected void OnTriedSpecial(String special, boolean exists){
			this.foundModel |= exists;
			logger.Info("Tested special Model: {}", CommandLogger.ItemData(special));
		}

		public void PrintConclusion(){
			if (foundModel)
				; // No-op
			else if (mainVariant == null)
				logger.Info("No variant could be computed for this item.");
			else {
				logger.Info("The item has a valid variant, but no associated model exists.");
				logger.PrintVariantIdTip(mainVariant);
			}
		}
	}
}
