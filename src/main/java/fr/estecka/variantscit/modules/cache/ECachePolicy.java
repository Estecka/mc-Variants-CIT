package fr.estecka.variantscit.modules.cache;

public enum ECachePolicy {
	/**
	 * The module should always be cached, even on its own.
	 */
	ALWAYS,
	/**
	 * The module is performant enough that it doesn't benefit from caching.
	 */
	AVOID,
	/**
	 * The module is a wrapper and shouldn't be cached on its own. Evaluate its
	 * content instead.
	 */
	UNWRAP,
	;
}
