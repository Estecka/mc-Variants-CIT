package fr.estecka.variantscit.assetgen;

import java.util.function.Supplier;
import net.minecraft.server.packs.resources.ResourceManager;
import fr.estecka.variantscit.mixin.ReloadableResourceManagerImplMixin;

/**
 * See {@linkplain ReloadableResourceManagerImplMixin}
 */
public class HotswappableResourceManager
{
	private ResourceManager resourceManager;
	private final Supplier<ResourceManager> refresher;

	public HotswappableResourceManager(ResourceManager initial, Supplier<ResourceManager> refresher){
		this.resourceManager = initial;
		this.refresher = refresher;
	}

	public ResourceManager Get(){
		return this.resourceManager;
	}

	public ResourceManager Refresh(){
		return (this.resourceManager = refresher.get());
	}
}
