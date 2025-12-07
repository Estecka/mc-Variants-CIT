package fr.estecka.variantscit.assetgen;

import fr.estecka.variantscit.reload.EAssetType;
import net.minecraft.util.Identifier;

public enum EAssetGenPass {
	BAKED_MODELS(1, EAssetType.TEXTURE,     EAssetType.BAKED_MODEL),
	ITEM_STATES (2, EAssetType.BAKED_MODEL, EAssetType.ITEM_STATE),
	;

	public final int order;
	public final EAssetType input, output;

	private EAssetGenPass(int order, EAssetType input, EAssetType output){
		this.order = order;
		this.input = input;
		this.output = output;
	}

	public Identifier GetOutputResourceId(Identifier assetId){
		return assetId.withPath(path -> output.directory+"/"+path+output.suffix);
	}
	
}
