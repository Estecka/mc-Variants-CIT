package fr.estecka.variantscit.mixin;

import java.util.List;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import com.llamalad7.mixinextras.sugar.Local;
import fr.estecka.variantscit.reload.ModuleLoader;
import fr.estecka.variantscit.VariantsCitMod;
import fr.estecka.variantscit.assetgen.GeneratorPresets;
import fr.estecka.variantscit.assetgen.HotswappableResourceManager;
import fr.estecka.variantscit.assetgen.TemplateRepository;
import net.minecraft.resource.LifecycledResourceManagerImpl;
import net.minecraft.resource.ReloadableResourceManagerImpl;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourceType;

/**
 * The  resource manager  breaks  its packs down  based on  namespace, so VCIT's
 * generated pack  ends up  being unable  to provide  any assets  due to it  not
 * containing any namespaces at all in the beginning. The resource manager needs
 * to be  re-instantiated  after each  asset-gen passes  to account  for the new
 * assets.
 * 
 * @implNote Running on the seemingly brittle assumption that this class is only
 * ever used for texture pack reloads. This is the case as of writing, but there
 * being a ResourceType variable bodes ill.
 */
@Unique
@Mixin(ReloadableResourceManagerImpl.class)
public class ReloadableResourceManagerImplMixin
{
	@Shadow private @Final ResourceType type;

	@ModifyArg(
		method="reload",
		index = 0,
		at=@At(
			value = "INVOKE",
			target = "net/minecraft/resource/SimpleResourceReload.start(Lnet/minecraft/resource/ResourceManager;Ljava/util/List;Ljava/util/concurrent/Executor;Ljava/util/concurrent/Executor;Ljava/util/concurrent/CompletableFuture;Z)Lnet/minecraft/resource/ResourceReload;"
		)
	)
	private ResourceManager reload(ResourceManager original, @Local(argsOnly=true) List<ResourcePack> packs){
		if (this.type != ResourceType.CLIENT_RESOURCES){
			VariantsCitMod.LOGGER.warn(
				"Something fundamental has changed in the way Minecraft reloads "
				+ "resources, and Variants-CIT ended up hooked to the wrong "
				+ "place: \"{}\".",
				this.type.getDirectory()
			);
			return original;
		}

		var hotswap = new HotswappableResourceManager(original, ()->new LifecycledResourceManagerImpl(this.type, packs));

		TemplateRepository.ReloadPatterns(original);
		GeneratorPresets.ReloadPresets(original);
		ModuleLoader.Result result = ModuleLoader.ReloadModules(hotswap);
		VariantsCitMod.OnResourceReload(result);

		return hotswap.Get();
	}
}
