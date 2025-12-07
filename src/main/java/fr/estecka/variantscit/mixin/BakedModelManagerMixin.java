package fr.estecka.variantscit.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.llamalad7.mixinextras.sugar.Local;
import fr.estecka.variantscit.reload.ModuleLoader;
import fr.estecka.variantscit.VariantsCitMod;
import fr.estecka.variantscit.assetgen.TemplateRepository;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.resource.ResourceManager;

@Unique
@Mixin(BakedModelManager.class)
public class BakedModelManagerMixin
{
	@Inject( method="reload", at=@At("HEAD") )
	private void reload(CallbackInfoReturnable<?> ci, @Local(argsOnly=true) ResourceManager manager){
		TemplateRepository.ReloadPatterns(manager);
		ModuleLoader.Result result = ModuleLoader.ReloadModules(manager);
		VariantsCitMod.OnResourceReload(result);
	}
}
