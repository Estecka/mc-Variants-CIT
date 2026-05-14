package fr.estecka.variantscit.trims;

import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer.TrimSpriteKey;
import net.minecraft.resources.ResourceLocation;

public interface ITrimSpriteKeyDuck
{
	ResourceLocation vcit$textureId(TrimPatternOverlay overlay);

	static public ResourceLocation GetTextureId(TrimSpriteKey original, TrimPatternOverlay override){
		return ((ITrimSpriteKeyDuck)(Object)original).vcit$textureId(override);
	}
}
