package fr.estecka.variantscit.modules.libraries;

import net.minecraft.resources.ResourceLocation;

public interface ILinearLibrary
{
	public ResourceLocation GetWithBias (int magnitude, int bias);
	public ResourceLocation GetOrLesser (int magnitude);
	public ResourceLocation GetOrGreater(int magnitude);
}
