package fr.estecka.variantscit.modules.impl;

import fr.estecka.variantscit.api.ISimpleCitModule;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.JukeboxPlayable;

public class MusicDiscModule
implements ISimpleCitModule
{
	@Override
	public ResourceLocation GetItemVariant(ItemStack stack){
		JukeboxPlayable component = stack.get(DataComponents.JUKEBOX_PLAYABLE);
		if (component == null)
			return null;

		return component.song().key().location();
	}
}
