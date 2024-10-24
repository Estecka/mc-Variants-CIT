package fr.estecka.variantscit.mixin;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import fr.estecka.variantscit.VariantsCitMod;
import net.minecraft.client.render.model.ModelBaker;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.util.Identifier;

@Mixin(ModelBaker.class)
public class ModelBakerMixin
{
	@Shadow private @Final @Mutable Map<Identifier, UnbakedModel> resolvedModels;
	@Shadow private @Final @Mutable Map<ModelIdentifier, UnbakedModel> models;

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
	private void AddFromTexture(ModelIdentifier modelId, Identifier parent) {
		StringReader reader = new StringReader(ARBITRARY_MODEL.formatted(parent.toString(), modelId.id().toString()));
		JsonUnbakedModel model = JsonUnbakedModel.deserialize(reader);
		this.resolvedModels.put(modelId.id(), model);
		this.models.put(modelId, model);
	}

	@Inject( method="<init>", at=@At("TAIL") )
	private void AddVariantModels(Map<?,?> _0, Map<?,?> _1, UnbakedModel _2, CallbackInfo ci) {
		// Make mutable
		this.resolvedModels = new HashMap<>(this.resolvedModels);
		this.models = new HashMap<>(this.models);

		var models = VariantsCitMod.GetModelsToCreate();
		VariantsCitMod.LOGGER.info("Creating {} item models from textures...", models.size());
		for (var entry : VariantsCitMod.GetModelsToCreate().entrySet())
			this.AddFromTexture(entry.getKey(), entry.getValue());
	}
}
