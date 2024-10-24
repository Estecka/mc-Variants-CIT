package fr.estecka.variantscit.mixin;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import fr.estecka.variantscit.VariantsCitMod;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.render.model.BlockStatesLoader;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.util.Identifier;

@Mixin(BakedModelManager.class)
public class BakedModelManagerMixin
{
	/**
	 * I could probably use the constructor for JsonUnbakedModel instead, but it
	 * is unclear how parent-child inheritance works with it.
	 */
	static private final String ARBITRARY_MODEL = """
		{
			"parent": "%s",
			"textures": {
				"layer0": "%s"
			}
		}
	""";

	@Unique
	private UnbakedModel CreateFromTexture(ModelIdentifier modelId, Identifier parent) {
		StringReader reader = new StringReader(ARBITRARY_MODEL.formatted(parent.toString(), modelId.id().toString()));
		JsonUnbakedModel model = JsonUnbakedModel.deserialize(reader);
		return model;
	}


	@Inject( method="collect", at=@At("HEAD"))
	private void AddVariantModels(UnbakedModel missingModel, Map<Identifier, UnbakedModel> inputs, BlockStatesLoader.BlockStateDefinition definition, CallbackInfoReturnable<?> ci, @Local(argsOnly=true) LocalRef<Map<Identifier, UnbakedModel>> inputRef)
	{
		// Make mutable
		inputs = new HashMap<>(inputs);
		inputRef.set(inputs);

		var models = VariantsCitMod.GetModelsToCreate();
		VariantsCitMod.LOGGER.info("Creating {} item models from textures...", models.size());
		for (var entry : VariantsCitMod.GetModelsToCreate().entrySet())
			inputs.put(entry.getKey().id(), this.CreateFromTexture(entry.getKey(), entry.getValue()));
	}
}
