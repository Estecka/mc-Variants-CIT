package fr.estecka.variantscit.modules.impl;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.estecka.variantscit.CodecUtil;
import fr.estecka.variantscit.VariantsCitMod;
import fr.estecka.variantscit.commands.CommandLogger;
import fr.estecka.variantscit.commands.WalktroughLogger;
import fr.estecka.variantscit.itemdata.preconditions.IItemPrecondition;
import fr.estecka.variantscit.itemdata.transforms.impl.LogTransform;
import fr.estecka.variantscit.modules.IBakedModule;
import fr.estecka.variantscit.modules.cache.CacheKeySet;
import fr.estecka.variantscit.modules.cache.ECachePolicy;
import fr.estecka.variantscit.modules.libraries.IVariantLibrary;
import fr.estecka.variantscit.modules.libraries.VariantLibrary;
import fr.estecka.variantscit.reload.IUnbakedModule;
import fr.estecka.variantscit.util.VariantUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public record PredicatesModule(
	List<PredicatedModel> variants
)
implements IBakedModule
{
/******************************************************************************/
/* # Baking                                                                   */
/******************************************************************************/

	static private record ModelReference (
		boolean isModelMissing,
		boolean isIntrinsic,
		@NotNull Identifier variantId,
		@Nullable Identifier modelId
	){
		static public final MapCodec<ModelReference> MAPCODEC = CodecUtil.MapWithAlternative(
			Identifier.CODEC.fieldOf("variantId").xmap(ModelReference::VariantId, ModelReference::variantId),
			Identifier.CODEC.fieldOf("modelId").xmap(ModelReference::Intrinsic, ModelReference::modelId)
		);

		static public ModelReference VariantId(Identifier variantId){
			return new ModelReference(true, false, variantId, null);
		}

		static public ModelReference Intrinsic(Identifier modelId){
			return new ModelReference(true, true, VariantUtil.IntrinsicVariantId(modelId), modelId);
		}

		public ModelReference Resolve(IVariantLibrary library){
			boolean missing = !library.HasVariantModel(variantId);
			Identifier resolvedModelId = library.GetVariantModel(this.variantId);

			if (!missing && this.modelId != null && !this.modelId.equals(resolvedModelId)){
				VariantsCitMod.LOGGER.error(
					"Somehow, an intrinsic model did not match. Please report this issue."
					+ "\nMissing: {} | Resolved: {}"
					+ "\nDefinition: {}"
					+ "\n{}",
					missing,
					resolvedModelId,
					this,
					ExceptionUtils.getStackTrace(new AssertionError())
				);
			}

			return new ModelReference(missing, this.isIntrinsic, this.variantId, resolvedModelId);
		}

		public String GetLabel(){
			if (this.isIntrinsic)
				return this.modelId.toString();
			else
				return '@' + this.variantId.toString();
		}
	}

	static private record PredicatedModel(
		ModelReference model,
		IItemPrecondition precondition
	) {
		static public final MapCodec<PredicatedModel> MAPCODEC = RecordCodecBuilder.mapCodec(builder->
			builder.group(
				ModelReference.MAPCODEC.forGetter(PredicatedModel::model),
				IItemPrecondition.CODEC.fieldOf("precondition").forGetter(PredicatedModel::precondition)
			)
			.apply(builder, PredicatedModel::new)
		);

		public PredicatedModel Resolve(IVariantLibrary library){
			return new PredicatedModel(model.Resolve(library), precondition);
		}
	}

	public record Parameters(
		List<PredicatedModel> predicates
	)
	implements IUnbakedModule
	{
		static public final MapCodec<Parameters> MAPCODEC = PredicatedModel.MAPCODEC.codec()
			.listOf()
			.fieldOf("predicates")
			.xmap(Parameters::new, Parameters::predicates)
			;

		@Override
		public boolean AcceptsVariant(Identifier variantId) {
			for (var p : predicates)
				if (p.model.variantId.equals(variantId))
					return true;
			return false;
		}

		@Override
		public Set<Identifier> GetIntrinsicModels() {
			return predicates.stream()
				.filter(p -> p.model.isIntrinsic)
				.map(p -> p.model.modelId)
				.distinct()
				.collect(Collectors.toSet())
				;
		}

		@Override
		public boolean AcceptsIntrinsic(Identifier modelId) {
			return predicates.stream()
				.filter(p -> modelId.equals(p.model.modelId))
				.findAny()
				.isPresent()
				;
		}

		@Override
		public IBakedModule Bake(VariantLibrary library) {
			return new PredicatesModule(
				this.predicates.stream()
					.map(p -> p.Resolve(library))
					.toList()
			);
		}
	}


/******************************************************************************/
/* # Caching                                                                  */
/******************************************************************************/

	@Override
	public CacheKeySet GetCacheKeys() {
		return CacheKeySet.OfList(this.variants.stream().map(PredicatedModel::precondition).toList());
	}

	@Override
	public ECachePolicy GetCachePolicy() {
		return ECachePolicy.ALWAYS;
	}


/******************************************************************************/
/* # Runtime                                                                  */
/******************************************************************************/

	@Override
	public Identifier GetModelForItem(ItemStack stack) {
		for (PredicatedModel v : variants) {
			if (v.precondition.Matches(stack))
				return v.model.modelId;
		}

		return null;
	}


/******************************************************************************/
/* # Debug                                                                    */
/******************************************************************************/

	// TODO
	@Override
	public void Summary(CommandLogger logger) {
		logger.Info(ChatFormatting.GOLD, "This module has no summary information at this time.");
	}

	@Override
	public Identifier Walkthrough(WalktroughLogger logger, ItemStack stack) {
		logger.Info("Predicates:");

		int i = 0;
		ModelReference match = null;
		for (PredicatedModel v : variants) {
			boolean matched = LogTransform.WithLogger(logger, ()->v.precondition.Matches(stack));
			logger.Info(" [{}] {}: {}",
				i++,
				CommandLogger.PackData(v.model.GetLabel()),
				(matched) ? Component.literal("Matched") : Component.literal("Failed").withStyle(ChatFormatting.GRAY)
			);
			if (matched){
				match = v.model;
				break;
			}
		}

		logger.Info("-");

		if (match != null) {
			if (match.isIntrinsic)
				logger.Info("Picked model ID: {}", CommandLogger.ItemData(match.variantId));
			else
				logger.Info("Picked variant ID: {}", CommandLogger.ItemData(match.variantId));
			
			if (match.isModelMissing){
				logger.Info("This model is missing.");

				if (match.isIntrinsic)
					logger.PrintPlainModelTip(match.modelId);
				else
					logger.PrintVariantIdTip(match.variantId);
			}

			return match.modelId;
		}
		else
			return null;
	}
}
