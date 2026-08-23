package fr.estecka.variantscit.modules.impl;

import java.util.List;
import java.util.Optional;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.estecka.variantscit.CodecUtil;
import fr.estecka.variantscit.VCitRegistries;
import fr.estecka.variantscit.commands.BufferedCommandLogger;
import fr.estecka.variantscit.commands.CommandLogger;
import fr.estecka.variantscit.commands.WalktroughLogger;
import fr.estecka.variantscit.itemdata.preconditions.IItemPrecondition;
import fr.estecka.variantscit.modules.IBakedModule;
import fr.estecka.variantscit.modules.PreconditionModule;
import fr.estecka.variantscit.modules.cache.CacheKeySet;
import fr.estecka.variantscit.modules.cache.ECachePolicy;
import fr.estecka.variantscit.modules.libraries.VariantLibrary;
import fr.estecka.variantscit.reload.IUnbakedModule;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public record GroupModule(
	VariantLibrary commonLibrary,
	String[] subPrefixes,
	IBakedModule[] submodules
)
implements IBakedModule
{
/******************************************************************************/
/* # Baking                                                                   */
/******************************************************************************/

	static public record SubModuleDefinition(
		Optional<String> modelPrefix,
		Optional<IItemPrecondition> precondition,
		IUnbakedModule parameters
	) {
		// TODO: Reduce code duplication with ModuleDefinition
		static public final Codec<SubModuleDefinition> CODEC = RecordCodecBuilder.create(builder->
			builder.group(
				CodecUtil.LEGACY_ITEM_PATH.optionalFieldOf("modelPrefix").forGetter(SubModuleDefinition::modelPrefix),
				IItemPrecondition.CODEC.optionalFieldOf("precondition").forGetter(SubModuleDefinition::precondition),
				VCitRegistries.MODULES.mapCodec.forGetter(SubModuleDefinition::parameters)
			)
			.apply(builder, SubModuleDefinition::new)
		);
	}

	static public record Unbaked(List<SubModuleDefinition> submodules)
	implements IUnbakedModule
	{
		static public final MapCodec<Unbaked> MAPCODEC = SubModuleDefinition.CODEC
			.listOf()
			.fieldOf("submodules")
			.xmap(Unbaked::new, Unbaked::submodules)
			;

		@Override
		public IBakedModule Bake(VariantLibrary library)
		{
			String[] prefixes = new String[submodules.size()];
			IBakedModule[] result = new IBakedModule[submodules.size()];
			for (int i=0; i<result.length; ++i) {
				SubModuleDefinition unbaked = submodules.get(i);
				VariantLibrary subLibrary = unbaked.modelPrefix.map(pfx->library.GetSubLibrary(pfx)).orElse(library);
				IBakedModule baked = unbaked.parameters.Bake(subLibrary);
				if (unbaked.precondition.isPresent())
					baked = new PreconditionModule(unbaked.precondition.get(), baked);

				prefixes[i] = unbaked.modelPrefix.orElse("");
				result[i] = baked;
			}
			return new GroupModule(library, prefixes, result);
		}

		@Override
		public boolean AcceptsVariant(Identifier variantId) {
			for (SubModuleDefinition m : submodules) {
				Identifier subVariant = variantId;
				if (m.modelPrefix.isPresent()){
					if (!subVariant.getPath().startsWith(m.modelPrefix.get()))
						continue;
					else
						subVariant = variantId.withPath(path->path.substring(m.modelPrefix.get().length()));
				}
				if (m.parameters.AcceptsVariant(subVariant))
					return true;
			}

			return false;
		}
	}

	@Override
	public CacheKeySet GetCacheKeys() {
		return CacheKeySet.OfCacheables(submodules);
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
		for (IBakedModule m : submodules) {
			Identifier result = m.GetModelForItem(stack);
			if (result != null) return result;
		}

		return null;
	}


/******************************************************************************/
/* # Debug                                                                    */
/******************************************************************************/

	@Override
	public void Summary(CommandLogger logger) {
		logger.Info("### Parent:");
		commonLibrary.Summary(logger);
		logger.Info("-");

		int i = 0;
		for (IBakedModule m : submodules) {
			logger.Info("### Submodule [{}]", i++);
			logger.labels.push(i);
			m.Summary(logger);
			logger.labels.pop();
			logger.Info("-");
		}
	}

	@Override
	public boolean VariantIdInfo(CommandLogger logger, Identifier variantId) {
		BufferedCommandLogger buffer = new BufferedCommandLogger(logger.commandContext);
		boolean result = false;

		int i = 0;
		for (IBakedModule m : submodules) {
			boolean r = m.VariantIdInfo(buffer, variantId);
			result |= r;

			if (!buffer.IsEmpty()) {
				logger.Info("### Submodule [{}]", i);
				logger.labels.push(i);
				buffer.Flush(logger);
				logger.labels.pop();
			}

			++i;
		}

		return result;
	}

	@Override
	public void Dump(CommandLogger logger) {
		commonLibrary.Dump(logger);
	}

	@Override
	public Identifier Walkthrough(WalktroughLogger logger, ItemStack stack) {
		for (int i=0; i<submodules.length; ++i) {
			logger.Info("### Submodule [{}]", i);
			Identifier r = submodules[i].Walkthrough(logger.WithSubPrefix(subPrefixes[i]), stack);
			logger.Info("-");
			if (r != null) break;
		}

		return this.GetModelForItem(stack);
	}

}
