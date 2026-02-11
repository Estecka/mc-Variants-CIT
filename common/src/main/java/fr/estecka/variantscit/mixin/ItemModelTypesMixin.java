package fr.estecka.variantscit.mixin;

import java.util.Optional;
import java.util.function.Function;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.render.item.model.ItemModel;
import net.minecraft.client.render.item.model.ItemModelTypes;

@Mixin(ItemModelTypes.class)
public class ItemModelTypesMixin
{
	@ModifyExpressionValue(method="<clinit>", remap=false, at=@At(value="INVOKE", target="com/mojang/serialization/Codec.dispatch(Ljava/util/function/Function;Ljava/util/function/Function;)Lcom/mojang/serialization/Codec;"))
	static private Codec<ItemModel.Unbaked> ModelOverrideCodec(Codec<ItemModel.Unbaked> original){
		return RecordCodecBuilder.create( instance ->
			instance.group(
				original.optionalFieldOf("variants-cit:override").forGetter(__->Optional.empty()),
				MapCodec.assumeMapUnsafe(original).forGetter(Function.identity())
			)
			.apply(instance, Optional::orElse)
		);
	}
}
