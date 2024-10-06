package fr.estecka.variantscit.mixin;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.item.ItemModels;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.item.ItemStack;
import fr.estecka.variantscit.BakedModuleRegistry;
import fr.estecka.variantscit.IItemModelProvider;

@Mixin(ItemRenderer.class)
@Environment(EnvType.CLIENT)
public class ItemRendererMixin
{
	@Unique
	private BakedModel	GetVariantModel(BakedModelManager modelManager, ItemStack stack, Supplier<BakedModel> original)
	{
		final IItemModelProvider module = BakedModuleRegistry.GetForItem(stack.getItem());
		ModelIdentifier modelId;

		if (module == null || (modelId=module.GetModelForItem(stack)) == null)
			return original.get();

		modelId = GetRecursiveModelOverride(stack, modelId, new HashSet<>());
		BakedModel model = modelManager.getModel(modelId);
		return (model != null) ? model : modelManager.getMissingModel();
	}

	@Unique
	private ModelIdentifier GetRecursiveModelOverride(ItemStack stack, ModelIdentifier targetModel, Set<IItemModelProvider> recursion)
	{
		IItemModelProvider module = BakedModuleRegistry.GetForModel(targetModel.id());
		if (module==null || recursion.contains(module))
			return targetModel;

		recursion.add(module);

		ModelIdentifier result = module.GetModelForItem(stack);
		if (result == null)
			return targetModel;
		else
			return GetRecursiveModelOverride(stack, result, recursion);
	}

	// Most items
	@WrapOperation( method="getModel", require=1, at=@At( value="INVOKE", target="net/minecraft/client/render/item/ItemModels.getModel (Lnet/minecraft/item/ItemStack;)Lnet/minecraft/client/render/model/BakedModel;") )
	private BakedModel	GetItemStackModel(ItemModels models, ItemStack stack, Operation<BakedModel> original) {
		return GetVariantModel(models.getModelManager(), stack, ()->original.call(models, stack));
	}

	// Trident and Spyglass
	@WrapOperation( method="renderItem", require=2, at=@At( value="INVOKE", target="net/minecraft/client/render/model/BakedModelManager.getModel (Lnet/minecraft/client/util/ModelIdentifier;)Lnet/minecraft/client/render/model/BakedModel;") )
	private BakedModel	GetIdentifiedModel(BakedModelManager models, ModelIdentifier id, Operation<BakedModel> original, ItemStack stack) {
		return GetVariantModel(models, stack, ()->original.call(models, id));
	}

}
