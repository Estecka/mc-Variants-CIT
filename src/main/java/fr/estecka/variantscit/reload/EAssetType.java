package fr.estecka.variantscit.reload;

import java.util.Optional;
import net.minecraft.resources.ResourceLocation;

public enum EAssetType
{
	ITEM_TEXTURE  (EModuleHook.ITEM_MODEL,   false, "textures", "item", ".png"),
	BAKED_MODEL   (EModuleHook.ITEM_MODEL,   false, "models",   "item", ".json"),
	ITEM_STATE    (EModuleHook.ITEM_MODEL,   true , "items",    ""     , ".json"),

	EQUIP_TEXTURE (EModuleHook.EQUIPPABLE,   false, "textures",  "entity/equipment", ".png"),
	EQUIPMENT     (EModuleHook.EQUIPPABLE,   true , "equipment", ""                 , ".json"),

	TRIM_TEXTURE  (EModuleHook.TRIM_PATTERN, false, "textures",                  "trims/entity",     ".png"),
	TRIM_MODEL    (EModuleHook.TRIM_PATTERN, true , "variants-cit/trim_pattern", "",                 ".json"),
	;

	public final EModuleHook hook;
	/**
	 * Fundamental asset types are the ones  that are actually used within their
	 * attributed hook. Other asset types may only be collected  for the purpose
	 * of asset  generation, and  must always  result  in the  generation  of an
	 * equivalent fundamental asset somewhere down the line.
	 */
	public final boolean isFundamental;
	public final String packDirectory;
	public final String identifierPrefix;
	public final String suffix;

	private EAssetType(EModuleHook hook, boolean isFundamental, String rootDir, String prefix, String suffix){
		this.hook = hook;
		this.isFundamental = isFundamental;
		this.suffix = suffix;
		this.packDirectory    = prefix.isEmpty() ? rootDir : rootDir+'/'+prefix;
		this.identifierPrefix = prefix.isEmpty() ? prefix  : prefix+'/';
	}

	public Optional<ResourceLocation> GetModelId(ResourceLocation resourceId){
		String path = resourceId.getPath();
		if (!path.startsWith(packDirectory+"/") || !path.endsWith(suffix))
			return Optional.empty();
		else
			return Optional.of(resourceId.withPath(path.substring(
				packDirectory.length() + 1,
				path.length() - suffix.length()
			)));
	}

	public ResourceLocation GetVanillaId(ResourceLocation shortId){
		if (identifierPrefix == null)
			return shortId;
		else
			return shortId.withPath(path -> identifierPrefix+path);
	}
}
