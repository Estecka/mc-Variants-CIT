package fr.estecka.variantscit.modules.libraries;

import java.util.Map;
import org.jetbrains.annotations.Nullable;
import fr.estecka.variantscit.api.IVariantManager;
import net.minecraft.util.Identifier;

public record VariantLibrary(
	@Nullable Identifier fallbackModel,
	Map<Identifier, Identifier> variantModels,
	Map<String, Identifier> specialModels
)
implements IVariantManager
{
	@Override
	public boolean HasVariantModel(Identifier variant){
		return this.variantModels.containsKey(variant);
	}

	@Override
	public @Nullable Identifier GetVariantModel(Identifier variant){
		if (variant == null)
			return null;
		else
			return this.variantModels.getOrDefault(variant, this.fallbackModel);
	}

	@Override
	public @Nullable Identifier GetSpecialModel(String key){
		return this.specialModels.get(key);
	}

	public int GetVariantCount(){
		return this.variantModels.size();
	}

	public boolean isEmpty(){
		return this.variantModels.isEmpty()
		    && this.specialModels.isEmpty()
		    && this.fallbackModel == null
		    ;
	}
}
