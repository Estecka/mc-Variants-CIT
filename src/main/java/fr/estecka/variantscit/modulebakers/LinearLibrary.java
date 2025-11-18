package fr.estecka.variantscit.modulebakers;

import org.jetbrains.annotations.Nullable;
import fr.estecka.variantscit.LinearSnapMap;
import fr.estecka.variantscit.VariantLibrary;
import fr.estecka.variantscit.commands.CommandLogger;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
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
					logger.Info("This module handles {} variants.", this.library.modelLine.Size());
				};
				@Override
				public void Dump(CommandLogger logger) {
					for (LinearSnapMap.Entry<Identifier> entry : this.library.modelLine){
						logger.Info(
							Text.empty()
							    .append(Text.literal(String.valueOf(entry.magnitude())).formatted(Formatting.AQUA))
							    .append(" -> ")
							    .append(Text.literal(entry.value().toString()).formatted(Formatting.YELLOW))
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
		return module.GetItemModel(stack, new SnitchingLinearLibrary(logger, this, module.GetNamespace()));
	}

	private class SnitchingLinearLibrary
	extends LinearLibrary
	{
		private final CommandLogger logger;
		private final String namespace;

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
			logger.Info(
				Text.literal("Getting model ")
				    .append(CommandLogger.VariantName(magnitude))
				    .append(" or "+textBias)
			);
			Identifier model = super.modelLine.GetClosestValue(magnitude, bias);

			if (model == null){
				logger.Info("No such model exists.");
				this.logger.PrintVariantIdTip(Identifier.of(namespace, String.valueOf(magnitude)));
			}
		}
	}

}
