package fr.estecka.variantscit.mixin;

import java.io.StringReader;
import java.util.Map;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import fr.estecka.variantscit.VariantsCitMod;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.render.model.ModelBaker;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

@Mixin(ModelBaker.class)
public class ModelBakerMixin
{
	@Shadow private @Final Map<Identifier, UnbakedModel> resolvedModels;
	@Shadow private @Final Map<ModelIdentifier, UnbakedModel> models;

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

	/**
	 * Injected right before the post-processing of `modelsToBake`.
	 */
	@Inject( method="<init>", at=@At(value="INVOKE", target="java/util/Map.values ()Ljava/util/Collection;") )
	private void	AddVariantModels(BlockColors _0, Profiler profiler, Map<?,?> _2, Map<?,?> _3, CallbackInfo ci)
	{
		profiler.swap("variants-cit");
		var models = VariantsCitMod.GetModelsToCreate();
		VariantsCitMod.LOGGER.info("Creating {} item models from textures...", models.size());
		for (var entry : VariantsCitMod.GetModelsToCreate().entrySet())
			this.AddFromTexture(entry.getKey(), entry.getValue());
	}
}
