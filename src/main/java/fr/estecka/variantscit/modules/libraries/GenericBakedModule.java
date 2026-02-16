package fr.estecka.variantscit.modules.libraries;

import java.util.Collection;
import fr.estecka.variantscit.commands.CommandLogger;
import fr.estecka.variantscit.modules.IBakedModule;
import fr.estecka.variantscit.modules.cache.MultiPropertyCache.ICacheablePropertySource;
import fr.estecka.variantscit.modules.libraries.IDebuggableLibrary.Snitch;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class GenericBakedModule<L>
implements IBakedModule
{
	public final L library;
	private final IDebuggableLibrary<L> debug;
	public final IGenericCitModule<L> logic;

	public GenericBakedModule(IDebuggableLibrary<L> library, IGenericCitModule<L> logic){
		this.library = library.GetLibrary();
		this.debug = library;
		this.logic = logic;
	}

	@Override
	public ResourceLocation GetModelForItem(ItemStack stack) {
		return logic.GetItemModel(stack, library);
	}

	@Override
	public Collection<ICacheablePropertySource> GetCacheSources() {
		return null;
	}

	@Override
	public void Summary(CommandLogger logger) {
		debug.Summary(logger);
	}

	@Override
	public void Dump(CommandLogger logger) {
		debug.Dump(logger);
	}

	@Override
	public ResourceLocation Walkthrough(CommandLogger logger, ItemStack stack) {
		Snitch<L> snitch = this.debug.CreateSnitch(logger);
		ResourceLocation result = this.logic.Walkthrough(stack, snitch.GetLibrary(), logger);
		snitch.PrintConclusion();
		return result;
	}
}
