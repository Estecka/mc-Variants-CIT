package fr.estecka.variantscit;

import java.util.Map;
import org.jetbrains.annotations.Nullable;
import fr.estecka.variantscit.api.ICitModule;
import fr.estecka.variantscit.api.IVariantManager;
import fr.estecka.variantscit.commands.CommandLogger;
import fr.estecka.variantscit.modulebakers.GenericBakedModule;
import fr.estecka.variantscit.modulebakers.IBakedModule;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public record VariantLibrary(
	@Nullable Identifier fallbackModel,
	Map<Identifier, Identifier> variantModels,
	Map<String, Identifier> specialModels
)
implements IVariantManager
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
		return new GenericBakedModule<>(this, logic){
			@Override public void Dump(CommandLogger logger) { VariantLibrary.this.Dump(logger); }
			@Override public void Summary(CommandLogger logger) { VariantLibrary.this.Summary(logger); }
			@Override public Identifier Walkthrough(CommandLogger logger, ItemStack stack) { return VariantLibrary.this.Walkthrough(logger, stack, logic); }
		};
	}


/******************************************************************************/
/* # Debug Commands                                                           */
/******************************************************************************/

	public void Summary(CommandLogger logger){
		logger.Info("This module handles "+this.variantModels.size()+" variants.");
	}

	public void Dump(CommandLogger logger){
		if (this.variantModels.isEmpty())
			logger.Info("This module does not have any variant.");
		else for (var entry : this.variantModels.entrySet())
		{
			Text variant = Text.literal(entry.getKey().toString()).formatted(Formatting.AQUA);
			Text model   = Text.literal(entry.getValue().toString()).formatted(Formatting.YELLOW);
			logger.Info(Text.empty()
				.append(variant)
				.append(Text.literal(" -> "))
				.append(model)
			);
		}
	}

	public Identifier Walkthrough(CommandLogger logger, ItemStack stack, ICitModule logic){
		return new SnitchingLibrary(this, logger).Walkthrough(stack, logic);
	}

	static private class SnitchingLibrary
	implements IVariantManager
	{

		private final VariantLibrary original;
		private final CommandLogger logger;
		private Identifier firstVariantId = null;
		private boolean foundVariantModel = false;

		SnitchingLibrary (VariantLibrary original, CommandLogger logger){
			this.original = original;
			this.logger = logger;
		}

		public Identifier Walkthrough(ItemStack stack, ICitModule logic){
			Identifier r = logic.GetItemModel(stack, this);
			if (firstVariantId == null)
				logger.Info("No variant ID could be computed for this item.");
			else if (!foundVariantModel){
				logger.Info("The item has a variant ID, but no associated model exists.");
				logger.Error("TODO: add tips.");
			}
			return r;
		}

		@Override
		public boolean HasVariantModel(Identifier variantId) {
			boolean r = original.HasVariantModel(variantId);
			logger.Info(Text.literal("Tested variant ID: ")
				.append(Text.literal(variantId.toString()).formatted(Formatting.AQUA))
			);
			foundVariantModel |= r;
			if (firstVariantId == null)
				firstVariantId = variantId;
			return r;
		}

		@Override
		public @Nullable Identifier GetVariantModel(Identifier variantId) {
			this.HasVariantModel(variantId);
			return original.GetVariantModel(variantId);
		}

		@Override
		public @Nullable Identifier GetSpecialModel(String key) {
			logger.Info("Tested special model: "+key);
			return original.GetSpecialModel(key);
		}
	}

}
