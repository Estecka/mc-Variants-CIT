package fr.estecka.variantscit.modules;

import fr.estecka.variantscit.api.ISimpleCitModule;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.JukeboxPlayableComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class MusicDiscModule
implements ISimpleCitModule
{
	@Override
	public Identifier GetItemVariant(ItemStack stack){
		JukeboxPlayableComponent component = stack.get(DataComponentTypes.JUKEBOX_PLAYABLE);
		if (component == null)
			return null;

		return component.song().key().getValue();
	}
}
