package fr.estecka.variantscit.reload;

public enum EAssetType
{
	TEXTURE     (EModuleContext.ITEM_MODEL, "textures/item", ".png" ),
	BAKED_MODEL (EModuleContext.ITEM_MODEL, "models/item"  , ".json"),
	ITEM_STATE  (EModuleContext.ITEM_MODEL, "items"        , ".json"),
	EQUIPMENT   (EModuleContext.EQUIPPABLE, "equipment"    , ".json"),
	;

	public final EModuleContext context;
	public final String directory;
	public final String suffix;
	private EAssetType(EModuleContext context, String directory, String suffix){
		this.context = context;
		this.directory = directory;
		this.suffix = suffix;
	}
}
