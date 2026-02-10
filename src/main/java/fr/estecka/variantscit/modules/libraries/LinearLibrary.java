package fr.estecka.variantscit.modules.libraries;

import org.jetbrains.annotations.Nullable;
import fr.estecka.variantscit.modules.IModuleBaker;
import net.minecraft.resources.ResourceLocation;
import fr.estecka.variantscit.commands.CommandLogger;


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
/* # Baking                                                                   */
/******************************************************************************/

	static public interface ILinearCitModule
	extends IGenericCitModule<ILinearLibrary>
	{
		String GetNamespace();
	}

	static public <M extends ILinearCitModule> IModuleBaker<M> GetBaker(){
		return new IModuleBaker<>() {
			@Override
			public boolean AcceptVariant(ResourceLocation variantId, M parameters) {
				if (!variantId.getNamespace().equals(parameters.GetNamespace()))
					return false;

				try {
					Integer.parseUnsignedInt(variantId.getPath());
				}
				catch (NumberFormatException e){
					return false;
				}

				return true;
			};

			@Override
			public GenericBakedModule<ILinearLibrary> Bake(VariantLibrary library, M linearModule){
				return new GenericBakedModule<ILinearLibrary>(
					new LinearLibrary(library, linearModule.GetNamespace()),
					linearModule
				);
			};
		};
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
	public Snitch<ILinearLibrary> CreateSnitch(CommandLogger logger) {
		return new SnitchingLinearLibrary(logger);
	}

	private class SnitchingLinearLibrary
	extends IDebuggableLibrary.Snitch<ILinearLibrary>
	implements ILinearLibrary
	{

		public SnitchingLinearLibrary(CommandLogger logger){
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
