package fr.estecka.variantscit.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import fr.estecka.variantscit.assetgen.GeneratedResourcePack;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.repository.RepositorySource;

@Mixin(Minecraft.class)
public class MinecraftClientMixin
{
	@ModifyArg(
		method = "<init>",
		index = 0,
		at = @At(
			value="INVOKE",
			target="net/minecraft/server/packs/repository/PackRepository.<init>([Lnet/minecraft/server/packs/repository/RepositorySource;)V"
		)
	)
	private RepositorySource[] AddAssetGenPack(RepositorySource[] original){
		RepositorySource[] result = new RepositorySource[original.length + 1];
		for (int i=0; i<original.length; ++i)
			result[i] = original[i];
		result[original.length] = register->register.accept(GeneratedResourcePack.PROFILE);
		return result;
	}
}
