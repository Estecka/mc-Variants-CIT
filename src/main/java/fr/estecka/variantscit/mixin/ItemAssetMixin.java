package fr.estecka.variantscit.mixin;

import java.util.Optional;
import java.util.function.Function;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.item.ItemAsset;
import net.minecraft.client.render.item.model.ItemModel;
import net.minecraft.client.render.item.model.ItemModelTypes;

@Mixin(ItemAsset.class)
public class ItemAssetMixin
{
	@ModifyExpressionValue(method="<clinit>", remap=false, at=@At(value="INVOKE", target="com/mojang/serialization/codecs/RecordCodecBuilder.create(Ljava/util/function/Function;)Lcom/mojang/serialization/Codec;"))
	private Codec<ItemAsset> ModelOverrideCodec(Codec<ItemAsset> original){
		return RecordCodecBuilder.create( instance ->
			instance.group(
				MapCodec.assumeMapUnsafe(original).forGetter(Function.identity()),
				ItemModelTypes.CODEC.optionalFieldOf("variants-cit:model").forGetter(v->Optional.of(v.model()))
			)
			.apply(instance, ItemAssetMixin::OverrideModel)
		);
	}

	@Unique
	static private ItemAsset OverrideModel(ItemAsset original, Optional<ItemModel.Unbaked> override) {
		if (override.isEmpty())
			return original;
		else
			return new ItemAsset(override.get(), original.properties());
	}
}
