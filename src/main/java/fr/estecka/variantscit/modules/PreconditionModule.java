package fr.estecka.variantscit.modules;

import fr.estecka.variantscit.commands.CommandLogger;
import fr.estecka.variantscit.itemdata.preconditions.IItemPrecondition;
import fr.estecka.variantscit.itemdata.transforms.impl.LogTransform;
import fr.estecka.variantscit.modules.cache.CacheKeySet;
import fr.estecka.variantscit.modules.cache.ECachePolicy;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public record PreconditionModule(
	IItemPrecondition precondition,
	IBakedModule subModule
)
implements IBakedModule
{
	@Override
	public ResourceLocation GetModelForItem(ItemStack stack) {
		if (precondition.Matches(stack))
			return subModule.GetModelForItem(stack);
		else
			return null;
	}

	@Override
	public ECachePolicy GetCachePolicy() {
		return ECachePolicy.ALWAYS;
	}

	@Override
	public CacheKeySet GetCacheKeys() {
		return CacheKeySet.OfCacheables(this.precondition, this.subModule);
	}

	@Override
	public ResourceLocation Walkthrough(CommandLogger logger, ItemStack stack) {
		if (LogTransform.WithLogger(logger, ()->precondition.Matches(stack))){
			logger.Info("Precondition matched.");
			return subModule.Walkthrough(logger, stack);
		}
		else {
			logger.Info("Precondition failed.");
			return null;
		}
	}

	@Override
	public void Dump(CommandLogger logger) {
		subModule.Dump(logger);
	}

	@Override
	public void Summary(CommandLogger logger) {
		subModule.Summary(logger);
	}
}
