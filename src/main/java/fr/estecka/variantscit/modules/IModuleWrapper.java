package fr.estecka.variantscit.modules;

import java.util.List;

public interface IModuleWrapper
{
	int size();
	List<IBakedModule> Unwrap();
}
