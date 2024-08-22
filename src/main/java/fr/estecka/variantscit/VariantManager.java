package fr.estecka.variantscit;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.Nullable;
import fr.estecka.variantscit.api.ICitModule;
import fr.estecka.variantscit.api.IVariantManager;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.ResourceManager;
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
	private final Set<Identifier> specialModels = new HashSet<>();


	public VariantManager(ModuleDefinition definition, ICitModule module){
		this.module = module;
		this.variantsDir = definition.variantDirectory();
		this.fallbackModel = definition.fallbackModel().orElse(null);

		definition.specialModels().values().stream()
			.filter(id -> id!=null)
			.distinct()
			.forEach(specialModels::add);
			;
	}

	public @Nullable Identifier GetModelVariantForItem(ItemStack stack){
		Identifier modelId = this.variantModels.get(module.GetItemVariant(stack));
		return (modelId != null) ? modelId : this.fallbackModel;
	}

	public @Nullable Identifier GetModelForItem(ItemStack stack){
		return module.GetItemModel(stack, this);
	}

	public final int GetVariantCount(){
		return this.variantModels.size();
	}

	public final Set<Identifier> GetAllModels(){
		Set<Identifier> result = new HashSet<>();
		result.add(fallbackModel);
		result.addAll(this.variantModels.values());
		result.addAll(this.specialModels);
		return result;
	}

	public void ReloadVariants(ResourceManager manager){
		this.variantModels = new HashMap<>();
		
		String folder = "models/"+variantsDir;
		
		for (Identifier fileId : manager.findResources(folder, id -> id.getPath().endsWith(".json")).keySet())
		{
			String namespace = fileId.getNamespace();
			String modelName, variantName;
			
			modelName = fileId.getPath();
			modelName = modelName.substring("models/".length(), modelName.length()-".json".length());
			
			variantName = modelName.substring(variantsDir.length() + 1);
			
			this.variantModels.put(
				Identifier.of(namespace, variantName),
				Identifier.of(namespace, modelName)
			);
		}
	}
}
