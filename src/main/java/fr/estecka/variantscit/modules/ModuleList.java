package fr.estecka.variantscit.modules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.commons.lang3.exception.ExceptionUtils;
import fr.estecka.variantscit.modules.cache.ECachePolicy;
import fr.estecka.variantscit.VariantsCitMod;
import fr.estecka.variantscit.commands.CommandLogger;
import fr.estecka.variantscit.commands.WalktroughLogger;
import fr.estecka.variantscit.modules.cache.CacheKeySet;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public class ModuleList
extends ArrayList<IBakedModule>
implements IBakedModule, IModuleWrapper
{
	public ModuleList(){
		super();
	}

	public ModuleList(Collection<? extends IBakedModule> submodules){
		super(submodules.size());
		this.addAll(submodules);
	}

	public IBakedModule UnwrapIfSingle(){
		if (this.isEmpty())
			VariantsCitMod.LOGGER.error("Empty module list {}", ExceptionUtils.getStackTrace(new IllegalArgumentException()));

		if (this.size() == 1)
			return this.getFirst();
		else
			return this;
	}

	@Override
	public List<IBakedModule> Unwrap() {
		return this;
	}

	@Override
	public Identifier GetModelForItem(ItemStack stack) {
		for (IBakedModule m : this){
			Identifier result = m.GetModelForItem(stack);
			if (result != null) return result;
		}
		return null;
	}

	@Override
	public IBakedModule Crawl(CommandLogger logger, ItemStack stack, boolean skip) {
		IBakedModule result = null;

		// logger.Info("Entering list module: {}", Integer.toHexString(System.identityHashCode(this)));
		for (IBakedModule m : this){
			IBakedModule r = m.Crawl(logger, stack, skip);
			if (!skip && r != null){
				result = r;
				skip = true;
			};
		}

		return result;
	}

	@Override
	public CacheKeySet GetCacheKeys() {
		return CacheKeySet.OfList(this);
	}

	@Override
	public boolean add(IBakedModule module) {
		if (module instanceof ModuleList list)
			return this.addAll(list);
		else
			return super.add(module);
	}

	@Override
	public boolean addAll(Collection<? extends IBakedModule> list) {
		boolean r = false;
		for (IBakedModule module : list)
			r |= this.add(module);
		return r;
	}

	@Override
	public ECachePolicy GetCachePolicy() {
		return ECachePolicy.UNWRAP;
	}

	@Override
	public void Dump(CommandLogger logger) {
		logger.Error("List modules cannot be debbuged. Please report this issue.");
	}

	@Override
	public void Summary(CommandLogger logger) {
		logger.Error("List modules cannot be debbuged. Please report this issue.");
	}
	
	@Override
	public Identifier Walkthrough(WalktroughLogger logger, ItemStack stack) {
		logger.Error("List modules cannot be debbuged. Please report this issue.");
		return null;
	}
}
