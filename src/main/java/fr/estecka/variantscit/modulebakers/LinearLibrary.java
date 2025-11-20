package fr.estecka.variantscit.modulebakers;

import org.jetbrains.annotations.Nullable;
import fr.estecka.variantscit.LinearSnapMap;
import fr.estecka.variantscit.VariantLibrary;
import fr.estecka.variantscit.commands.CommandLogger;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;


public class LinearLibrary
{
	protected final Identifier fallback;
	protected final LinearSnapMap<Identifier> modelLine;

	protected LinearLibrary(Identifier fallback, LinearSnapMap<Identifier> modelLine){
		this.fallback = fallback;
		this.modelLine = modelLine;
	}

	public LinearLibrary(VariantLibrary variantLibrary, String allowedNamespace){
		this(variantLibrary.fallbackModel(), new LinearSnapMap<>());

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

	private Identifier Fallback(@Nullable Identifier id){
		return (id != null) ? id : fallback;
	}

	public Identifier GetOrLesser(int magnitude){
		return Fallback(this.modelLine.GetClosestValue(magnitude, -1));
	}

	public Identifier GetOrGreater(int magnitude){
		return Fallback(this.modelLine.GetClosestValue(magnitude, +1));
	}


/******************************************************************************/
/* # Baking                                                                   */
/******************************************************************************/

	static public interface ILinearCitModule
	extends IGenericCitModule<LinearLibrary>
	{
		String GetNamespace();
	}

	static public <T extends ILinearCitModule> IModuleBaker<T> GetBaker(){
		return (IModuleBaker<T>)BAKER;
	}

	static public final IModuleBaker<? extends ILinearCitModule> BAKER = new IModuleBaker<>()
	{
		@Override
		public boolean AcceptVariant(Identifier variantId, ILinearCitModule parameters) {
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
		public GenericBakedModule<LinearLibrary> Bake(VariantLibrary library, ILinearCitModule linearModule){
			return new GenericBakedModule<>(new LinearLibrary(library, linearModule.GetNamespace()), linearModule)
			{
				@Override
				public void Summary(CommandLogger logger) {
					logger.Info("This module handles {} variants.", this.library.modelLine.size());
				};
				@Override
				public void Dump(CommandLogger logger) {
					if (this.library.modelLine.size() <= 0)
						logger.Info("This module does not have any variant.");
					else for (LinearSnapMap.Entry<Identifier> entry : this.library.modelLine){
						logger.Info("{} -> {}",
							CommandLogger.ItemData(entry.magnitude()),
							CommandLogger.PackData(entry.value())
						);
					}
				};
				@Override
				public Identifier Walkthrough(CommandLogger logger, ItemStack stack) {
					return library.Walkthrough(logger, linearModule, stack);
				};
			};
		}
	};


/******************************************************************************/
/* # DebugCommands                                                            */
/******************************************************************************/
	public Identifier Walkthrough(CommandLogger logger, ILinearCitModule module, ItemStack stack){
		return new SnitchingLinearLibrary(logger, this, module.GetNamespace()).Walkthrough(logger, module, stack);
	}

	private class SnitchingLinearLibrary
	extends LinearLibrary
	{
		private final CommandLogger logger;
		private final String namespace;
		private Integer firstVariant = null;
		private boolean foundModel = false;

		public SnitchingLinearLibrary(CommandLogger logger, LinearLibrary original, String namespace){
			super(original.fallback, original.modelLine);
			this.logger = logger;
			this.namespace = namespace;
		}

		@Override
		public Identifier GetOrGreater(int magnitude) {
			LogGet(magnitude, +1, "greater", namespace);
			return super.GetOrGreater(magnitude);
		}

		@Override
		public Identifier GetOrLesser(int magnitude) {
			LogGet(magnitude, -1, "lesser", namespace);
			return super.GetOrLesser(magnitude);
		}

		private void LogGet(int magnitude, int bias, String textBias, String namespace){
			if (this.firstVariant == null)
				this.firstVariant = magnitude;
			logger.Info("Getting model {} or {}", CommandLogger.ItemData(magnitude), textBias);
			Identifier model = super.modelLine.GetClosestValue(magnitude, bias);

			if (model != null)
				this.foundModel = true;
		}

		@Override
		public Identifier Walkthrough(CommandLogger logger, ILinearCitModule module, ItemStack stack) {
			Identifier result = module.Walkthrough(stack, this, logger);

			if (firstVariant != null && !foundModel){
				logger.Info("The item has a valid variant, but no associated model exists.");
				this.logger.PrintVariantIdTip(Identifier.of(namespace, String.valueOf(firstVariant)));
			}
			
			return result;
		}
	}

}
