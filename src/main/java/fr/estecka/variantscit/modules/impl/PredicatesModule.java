package fr.estecka.variantscit.modules.impl;

import java.util.List;
import org.jetbrains.annotations.Nullable;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.estecka.variantscit.commands.CommandLogger;
import fr.estecka.variantscit.itemdata.preconditions.IItemPrecondition;
import fr.estecka.variantscit.modules.IBakedModule;
import fr.estecka.variantscit.modules.cache.CacheKeySet;
import fr.estecka.variantscit.modules.cache.ECachePolicy;
import fr.estecka.variantscit.modules.libraries.GenericBakedModule;
import fr.estecka.variantscit.modules.libraries.ISimpleCitModule;
import fr.estecka.variantscit.modules.libraries.IVariantLibrary;
import fr.estecka.variantscit.modules.libraries.VariantLibrary;
import fr.estecka.variantscit.reload.IUnbakedModule;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public record PredicatesModule(
	List<PredicatedVariant> variants
)
implements ISimpleCitModule
{
/******************************************************************************/
/* # Baking                                                                   */
/******************************************************************************/
	static public record PredicatedVariant(
		IItemPrecondition precondition,
		Identifier variantId
	) {
		static public final MapCodec<PredicatedVariant> MAPCODEC = RecordCodecBuilder.mapCodec(builder->
			builder.group(
				IItemPrecondition.CODEC.fieldOf("precondition").forGetter(PredicatedVariant::precondition),
				Identifier.CODEC.fieldOf("variantId").forGetter(PredicatedVariant::variantId)
			)
			.apply(builder, PredicatedVariant::new)
		);
	}

	public record Unbaked(
		PredicatesModule parameters
	)
	implements IUnbakedModule
	{
		static public final MapCodec<Unbaked> MAPCODEC = PredicatedVariant.MAPCODEC.codec()
			.listOf()
			.fieldOf("predicates")
			.xmap(PredicatesModule::new, PredicatesModule::variants)
			.xmap(Unbaked::new, Unbaked::parameters)
			;

		@Override
		public IBakedModule Bake(VariantLibrary library) {
			return new GenericBakedModule<>(library, parameters);
		}

		@Override
		public boolean AcceptsVariant(Identifier variantId) {
			for (var v : parameters.variants)
				if (v.variantId.equals(variantId))
					return true;
			return false;
		}
	}

	@Override
	public CacheKeySet GetCacheKeys() {
		return CacheKeySet.OfList(this.variants.stream().map(PredicatedVariant::precondition).toList());
	}

	@Override
	public ECachePolicy GetCachePolicy() {
		return ECachePolicy.ALWAYS;
	}


/******************************************************************************/
/* # Runtime                                                                  */
/******************************************************************************/

	@Override
	public @Nullable Identifier GetItemVariant(ItemStack stack) {
		for (PredicatedVariant v : variants) {
			if (v.precondition.Matches(stack))
				return v.variantId;
		}

		return null;
	}


/******************************************************************************/
/* # Debug                                                                    */
/******************************************************************************/

	@Override
	public @Nullable Identifier Walkthrough(ItemStack stack, IVariantLibrary library, CommandLogger logger) {
		logger.Info("Predicates:");
		int i = 0;

		for (PredicatedVariant v : variants) {
			boolean matched = v.precondition.Matches(stack);
			logger.Info(" [{}] {}: {}",
				i++,
				CommandLogger.PackData(v.variantId),
				(matched) ? Component.literal("Matched") : Component.literal("Failed").withStyle(ChatFormatting.RED)
			);
			if (matched)
				break;
		}

		logger.Info("-");
		return this.GetItemModel(stack, library);
	}
}
