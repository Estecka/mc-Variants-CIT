package fr.estecka.variantscit;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.Nullable;
import fr.estecka.variantscit.api.ICitModule;
import fr.estecka.variantscit.api.IVariantManager;
import net.fabricmc.fabric.impl.client.model.loading.ModelLoadingConstants;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

public final class VariantManager
implements IVariantManager, IItemModelProvider
{
	private final ICitModule module;
	private final String prefix;
	private final @Nullable ModelIdentifier fallbackModel;

	/**
	 * Maps variant IDs to model IDs.
	 */
	private final Map<Identifier, ModelIdentifier> variantModels = new HashMap<>();
	private final Map<String, ModelIdentifier> specialModels;


	public VariantManager(ModuleDefinition definition, ICitModule module){
		this.module = module;
		this.prefix = definition.modelPrefix();
		this.fallbackModel = definition.GetFallbackModelId();
		this.specialModels = definition.GetSpecialModelIds();
	}

	@Deprecated
	@Override
	public @Nullable ModelIdentifier GetModelVariantForItem(ItemStack stack){
		return GetVariantModel(module.GetItemVariant(stack));
	}

	@Override
	public @Nullable ModelIdentifier GetVariantModel(Identifier variant){
		if (variant == null)
			return null;
		else
			return this.variantModels.getOrDefault(variant, this.fallbackModel);
	}

	@Override
	public @Nullable ModelIdentifier GetSpecialModel(String key){
		return this.specialModels.get(key);
	}

	public @Nullable ModelIdentifier GetModelForItem(ItemStack stack){
		return module.GetItemModel(stack, this);
	}

	public final int GetVariantCount(){
		return this.variantModels.size();
	}

	public final Set<ModelIdentifier> GetAllModels(){
		Set<ModelIdentifier> result = new HashSet<>();
		result.add(fallbackModel);
		result.addAll(this.variantModels.values());
		result.addAll(this.specialModels.values());
		result.remove(null);
		return result;
	}

	public void ReloadVariants(ResourceManager manager){
		this.variantModels.clear();
		
		String folder = "models";
		if (prefix.contains("/")){
			folder += '/' + prefix.substring(0, prefix.lastIndexOf('/'));
		}

		for (Identifier fileId : manager.findResources(folder, id -> id.getPath().startsWith("models/"+prefix) && id.getPath().endsWith(".json")).keySet())
		{
			String namespace = fileId.getNamespace();
			String modelName, variantName;
			modelName = fileId.getPath();
			modelName = modelName.substring("models/".length(), modelName.length()-".json".length());
			variantName = modelName.substring(prefix.length());

			this.variantModels.put(
				Identifier.of(namespace, variantName),
				ModelLoadingConstants.toResourceModelId(Identifier.of(namespace, modelName))
			);
		}
	}
}
