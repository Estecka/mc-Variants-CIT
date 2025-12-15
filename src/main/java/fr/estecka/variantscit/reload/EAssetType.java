package fr.estecka.variantscit.reload;

public enum EAssetType
{
	ITEM_TEXTURE  (EModuleContext.ITEM_MODEL, false, "textures/item", ".png"),
	BAKED_MODEL   (EModuleContext.ITEM_MODEL, false, "models/item"  , ".json"),
	ITEM_STATE    (EModuleContext.ITEM_MODEL, true , "items"        , ".json"),

	EQUIP_TEXTURE (EModuleContext.EQUIPPABLE, false, "textures/entity/equipment", ".png"),
	EQUIPMENT     (EModuleContext.EQUIPPABLE, true , "equipment"                , ".json"),
	;

	public final EModuleContext context;
	/**
	 * Fundamental asset types are the ones  that are actually used within their
	 * attributed  context.  Other  asset types  may only  be collected  for the
	 * purpose of asset generation, and must always  result in the generation of
	 * an equivalent fundamental asset somewhere down the line.
	 */
	public final boolean isFundamental;
	public final String directory;
	public final String suffix;

	private EAssetType(EModuleContext context, boolean isFundamental, String directory, String suffix){
		this.context = context;
		this.isFundamental = isFundamental;
		this.directory = directory;
		this.suffix = suffix;
	}
}
