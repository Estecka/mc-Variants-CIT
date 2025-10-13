package fr.estecka.variantscit;

import fr.estecka.variantscit.modulebakers.IModuleBaker;

public record UnbakedModule<T>(
	IModuleBaker<T> baker,
	T parameters
){}
