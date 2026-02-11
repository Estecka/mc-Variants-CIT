package fr.estecka.variantscit.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import fr.estecka.variantscit.assetgen.GeneratedResourcePack;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourcePackProvider;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin
{
	@ModifyArg(
		method = "<init>",
		index = 0,
		at = @At(
			value="INVOKE",
			target="net/minecraft/resource/ResourcePackManager.<init>([Lnet/minecraft/resource/ResourcePackProvider;)V"
		)
	)
	private ResourcePackProvider[] AddAssetGenPack(ResourcePackProvider[] original){
		ResourcePackProvider[] result = new ResourcePackProvider[original.length + 1];
		for (int i=0; i<original.length; ++i)
			result[i] = original[i];
		result[original.length] = register->register.accept(GeneratedResourcePack.PROFILE);
		return result;
	}
}
