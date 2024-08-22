package fr.estecka.variantscit;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.Nullable;
import fr.estecka.variantscit.api.ICitModule;
import fr.estecka.variantscit.api.IVariantManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public final class VariantManager
implements IVariantManager
{
	private final ICitModule module;
	private final String variantsDir;
	private final @Nullable Identifier fallbackModel;

	/**
	 * Maps variant IDs to model IDs.
	 */
	private Map<Identifier, Identifier> variantModels = new HashMap<>();


	public VariantManager(ModuleDefinition definition, ICitModule module){
		this.module = module;
		this.variantsDir = definition.variantDirectory();
		this.fallbackModel = definition.fallbackModel().orElse(null);
	}

	public @Nullable Identifier GetModelVariantForItem(ItemStack stack){
		Identifier modelId = this.variantModels.get(module.GetItemVariant(stack));
		return (modelId != null) ? modelId : this.fallbackModel;
	}

	public @Nullable Identifier GetModelForItem(ItemStack stack){
		return module.GetModelForItem(stack, this);
	}

	public final int GetVariantCount(){
		return this.variantModels.size();
	}

	public final Set<Identifier> GetAllModels(){
		Set<Identifier> result = new HashSet<>();
		result.add(fallbackModel);
		result.addAll(this.variantModels.values());
		for (Identifier id : module.GetSpecialModels())
		if  (id != null)
			result.add(id);

		return result;
	}

	public final CitLoader GetModelLoader(){
		return new CitLoader(this.variantsDir, map->{this.variantModels = map;});
	}
}
