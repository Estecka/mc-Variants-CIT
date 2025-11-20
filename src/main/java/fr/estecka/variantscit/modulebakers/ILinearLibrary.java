package fr.estecka.variantscit.modulebakers;

import net.minecraft.util.Identifier;

public interface ILinearLibrary
{
	public Identifier GetOrLesser (int magnitude);
	public Identifier GetOrGreater(int magnitude);
}
