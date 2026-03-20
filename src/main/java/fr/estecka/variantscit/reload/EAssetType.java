package fr.estecka.variantscit.reload;

import java.util.Optional;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

public enum EAssetType
{
	ITEM_TEXTURE  (EModuleHook.ITEM_MODEL, false, "textures/item", ".png"),
	BAKED_MODEL   (EModuleHook.ITEM_MODEL, false, "models/item"  , ".json"),
	ITEM_STATE    (EModuleHook.ITEM_MODEL, true , "items"        , ".json"),

	EQUIP_TEXTURE (EModuleHook.EQUIPPABLE, false, "textures/entity/equipment", ".png"),
	EQUIPMENT     (EModuleHook.EQUIPPABLE, true , "equipment"                , ".json"),
	;

	public final EModuleHook hook;
	/**
	 * Fundamental asset types are the ones  that are actually used within their
	 * attributed hook. Other asset types may only be collected  for the purpose
	 * of asset  generation, and  must always  result  in the  generation  of an
	 * equivalent fundamental asset somewhere down the line.
	 */
	public final boolean isFundamental;
	public final @Nullable String vanillaPrefix;
	public final String directory;
	public final String suffix;

	private EAssetType(EModuleHook hook, boolean isFundamental, String directory, String suffix){
		this.hook = hook;
		this.isFundamental = isFundamental;
		this.directory = directory;
		this.suffix = suffix;

		int subDir = directory.indexOf("/");
		if (subDir >= 0)
			this.vanillaPrefix = directory.substring(subDir + 1);
		else
			this.vanillaPrefix = null;
	}

	public Optional<Identifier> GetShortId(Identifier resourceId){
		String path = resourceId.getPath();
		if (!path.startsWith(directory+"/") || !path.endsWith(suffix))
			return Optional.empty();
		else
			return Optional.of(resourceId.withPath(path.substring(
				directory.length() + 1,
				path.length() - suffix.length()
			)));
	}

	public Identifier GetVanillaId(Identifier shortId){
		if (vanillaPrefix == null)
			return shortId;
		else
			return shortId.withPath(path -> vanillaPrefix+"/"+path);
	}
}
