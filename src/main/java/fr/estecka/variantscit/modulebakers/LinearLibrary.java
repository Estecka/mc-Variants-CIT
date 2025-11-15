package fr.estecka.variantscit.modulebakers;

import org.jetbrains.annotations.Nullable;
import fr.estecka.variantscit.LinearSnapMap;
import fr.estecka.variantscit.VariantLibrary;
import fr.estecka.variantscit.commands.CommandLogger;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;


public class LinearLibrary
{
	private final Identifier fallback;
	private final LinearSnapMap<Identifier> modelLine = new LinearSnapMap<>();

	public LinearLibrary(VariantLibrary variantLibrary, String allowedNamespace){
		this.fallback = variantLibrary.fallbackModel();

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
		public GenericBakedModule<LinearLibrary> Bake(VariantLibrary library, ILinearCitModule logic){
			return new GenericBakedModule<>(new LinearLibrary(library, logic.GetNamespace()), logic)
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
			};
		}
	};

}
