package fr.estecka.variantscit.commands;

import java.util.function.Predicate;
import java.util.function.Supplier;
import fr.estecka.variantscit.mixin.ClientWorldAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;

public interface IClientEntitySelector
extends Supplier<Entity>
{
	static public final Minecraft client = Minecraft.getInstance();

	static public Iterable<Entity> AllEntities(){
		return ((ClientWorldAccessor)client.player.level()).invokeGetEntities().getAll();
	}

	static public Entity GetNearest(Predicate<Entity> isElligible){
		Entity nearest = null;
		double nearestDistance = Double.POSITIVE_INFINITY;
		for (Entity e : AllEntities())
		if (isElligible.test(e))
		{
			double eDistance = e.position().subtract(client.player.position()).lengthSqr();
			if (eDistance < nearestDistance){
				nearest = e;
				nearestDistance = eDistance;
			}
		}
		return nearest;
	}

	static public Entity GetSelf(){
		return client.player;
	}

	static public Entity GetGroundItem(){
		return GetNearest(e->e instanceof ItemEntity);
	}

	static public Entity GetPlayer(){
		return GetNearest(e->e != client.player && e instanceof Player);
	}

	static public Entity GetEntity(){
		return GetNearest(e->e != client.player);
	}

}
