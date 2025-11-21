package fr.estecka.variantscit;

import java.util.Map;
import org.jetbrains.annotations.Nullable;
import fr.estecka.variantscit.api.ICitModule;
import fr.estecka.variantscit.api.IVariantManager;
import fr.estecka.variantscit.commands.CommandLogger;
import fr.estecka.variantscit.modulebakers.GenericBakedModule;
import fr.estecka.variantscit.modulebakers.IBakedModule;
import fr.estecka.variantscit.modulebakers.IDebuggableLibrary;
import net.minecraft.util.Identifier;

public record VariantLibrary(
	@Nullable Identifier fallbackModel,
	Map<Identifier, Identifier> variantModels,
	Map<String, Identifier> specialModels
)
implements IVariantManager, IDebuggableLibrary<IVariantManager>
{
	@Override
	public boolean HasVariantModel(Identifier variant){
		return this.variantModels.containsKey(variant);
	}

	@Override
	public @Nullable Identifier GetVariantModel(Identifier variant){
		if (variant == null)
			return null;
		else
			return this.variantModels.getOrDefault(variant, this.fallbackModel);
	}

	@Override
	public @Nullable Identifier GetSpecialModel(String key){
		return this.specialModels.get(key);
	}

	public int GetVariantCount(){
		return this.variantModels.size();
	}

	public boolean isEmpty(){
		return this.variantModels.isEmpty()
		    && this.specialModels.isEmpty()
		    && this.fallbackModel == null
		    ;
	}

	public IBakedModule Bake(ICitModule logic){
		return new GenericBakedModule<>(this, logic);
	}


/******************************************************************************/
/* # Debug Commands                                                           */
/******************************************************************************/

	@Override
	public void Summary(CommandLogger logger){
		logger.Info("This module handles {} variants.", this.variantModels.size());
	}

	@Override
	public void Dump(CommandLogger logger){
		if (this.variantModels.isEmpty())
			logger.Info("This module does not have any variant.");
		else for (var entry : this.variantModels.entrySet())
		{
			logger.Info("{} -> {} ",
				CommandLogger.ItemData(entry.getKey()),
				CommandLogger.PackData(entry.getValue())
			);
		}
	}

	@Override
	public Snitch<IVariantManager> CreateSnitch(CommandLogger logger) {
		return new SnitchingLibrary(logger);
	}

	private class SnitchingLibrary
	extends IDebuggableLibrary.Snitch<IVariantManager>
	implements IVariantManager
	{
		public SnitchingLibrary (CommandLogger logger){
			super(logger);
		}

		@Override
		public boolean HasVariantModel(@Nullable Identifier variantId) {
			boolean r = VariantLibrary.this.HasVariantModel(variantId);
			this.OnTriedVariant(variantId, r);
			return r;
		}

		@Override
		public @Nullable Identifier GetVariantModel(Identifier variantId) {
			this.OnTriedVariant(variantId, this.HasVariantModel(variantId));
			return VariantLibrary.this.GetVariantModel(variantId);
		}

		@Override
		public @Nullable Identifier GetSpecialModel(String key) {
			Identifier r = VariantLibrary.this.GetSpecialModel(key);
			this.OnTriedSpecial(key, r != null);
			return r;
		}
	}

}
