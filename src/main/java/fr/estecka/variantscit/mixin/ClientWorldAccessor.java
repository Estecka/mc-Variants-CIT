package fr.estecka.variantscit.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.LevelEntityGetter;

@Mixin(ClientLevel.class)
public interface ClientWorldAccessor
{
	@Invoker LevelEntityGetter<Entity> invokeGetEntities();
}
