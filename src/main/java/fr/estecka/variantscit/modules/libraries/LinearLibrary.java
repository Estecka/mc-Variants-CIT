package fr.estecka.variantscit.modules.libraries;

import org.jetbrains.annotations.Nullable;
import net.minecraft.resources.ResourceLocation;
import fr.estecka.variantscit.commands.CommandLogger;
import fr.estecka.variantscit.commands.WalktroughLogger;


public class LinearLibrary
implements ILinearLibrary, IDebuggableLibrary<ILinearLibrary>
{
	protected final String namespace;
	protected final ResourceLocation fallback;
	protected final LinearSnapMap<ResourceLocation> modelLine = new LinearSnapMap<>();

	public LinearLibrary(VariantLibrary variantLibrary, String allowedNamespace){
		this.fallback = variantLibrary.fallbackModel();
		this.namespace = allowedNamespace;

		for (var variant : variantLibrary.variantModels().entrySet())
		if  (variant.getKey().getNamespace().equals(allowedNamespace))
		{
			try {
				int magnitude = Integer.parseInt(variant.getKey().getPath());
				this.modelLine.AddEntry(magnitude, variant.getValue());
			} catch (NumberFormatException err)
			{}
		}
	}

	private ResourceLocation Fallback(@Nullable ResourceLocation id){
		return (id != null) ? id : fallback;
	}

	public ResourceLocation GetWithBias(int magnitude, int bias){
		return Fallback(this.modelLine.GetClosestValue(magnitude, bias));
	}

	public ResourceLocation GetOrLesser(int magnitude){
		return GetWithBias(magnitude, -1);
	}

	public ResourceLocation GetOrGreater(int magnitude){
		return GetWithBias(magnitude, +1);
	}


/******************************************************************************/
/* # DebugCommands                                                            */
/******************************************************************************/

	@Override
	public void Summary(CommandLogger logger) {
		logger.Info("This module handles {} variants.", this.modelLine.size());
	}

	@Override
	public void Dump(CommandLogger logger) {
		if (this.modelLine.size() <= 0)
			logger.Info("This module does not have any variant.");
		else for (LinearSnapMap.Entry<ResourceLocation> entry : this.modelLine){
			logger.Info("{} -> {}",
				CommandLogger.ItemData(entry.magnitude()),
				CommandLogger.PackData(entry.value())
			);
		}
	}

	@Override
	public Snitch<ILinearLibrary> CreateSnitch(WalktroughLogger logger) {
		return new SnitchingLinearLibrary(logger);
	}

	private class SnitchingLinearLibrary
	extends IDebuggableLibrary.Snitch<ILinearLibrary>
	implements ILinearLibrary
	{

		public SnitchingLinearLibrary(WalktroughLogger logger){
			super(logger);
		}

		@Override
		public ResourceLocation GetWithBias(int magnitude, int bias) {
			if (bias < 0)
				return GetOrLesser(magnitude);
			if (bias > 0)
				return GetOrGreater(magnitude);

			ResourceLocation r = LinearLibrary.this.GetWithBias(magnitude, 0);
			LogGet(magnitude, r != null, "strictly equal", namespace);
			return r;
		}

		@Override
		public ResourceLocation GetOrGreater(int magnitude) {
			ResourceLocation r = LinearLibrary.this.GetOrGreater(magnitude);
			LogGet(magnitude, r != null, "greater", namespace);
			return r;
		}

		@Override
		public ResourceLocation GetOrLesser(int magnitude) {
			ResourceLocation r = LinearLibrary.this.GetOrLesser(magnitude);
			LogGet(magnitude, r != null, "lesser", namespace);
			return r;
		}

		private void LogGet(int magnitude, boolean exists, String textBias, String namespace){
			this.OnTriedVariant(ResourceLocation.fromNamespaceAndPath(namespace, String.valueOf(magnitude)), exists);
			logger.Info("Getting model {} or {}", CommandLogger.ItemData(magnitude), textBias);
		}
	}

}
