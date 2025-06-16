package fr.estecka.variantscit.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import fr.estecka.variantscit.IItemModelProvider;
import fr.estecka.variantscit.VariantsCitMod;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.feature.CapeFeatureRenderer;
import net.minecraft.client.render.entity.feature.ElytraFeatureRenderer;
import net.minecraft.client.render.entity.feature.HorseArmorFeatureRenderer;
import net.minecraft.client.render.entity.feature.LlamaDecorFeatureRenderer;
import net.minecraft.client.render.entity.feature.WolfArmorFeatureRenderer;
import net.minecraft.component.type.EquippableComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

@Mixin({
	ArmorFeatureRenderer.class,
	// CapeFeatureRenderer.class,
	// ElytraFeatureRenderer.class,
	// HorseArmorFeatureRenderer.class,
	// LlamaDecorFeatureRenderer.class,
	// WolfArmorFeatureRenderer.class,
})
public class FeatureRendererMixin
{

	@ModifyExpressionValue(
		at=@At(value="INVOKE", target="net/minecraft/item/ItemStack.get(Lnet/minecraft/component/ComponentType;)Ljava/lang/Object;"),
		require = 1,
		method = {
			/* ArmorFeatureRenderer:renderArmor */ "renderArmor",
			// /* ArmorFeatureRenderer:hasModel */ "hasModel",
			// /* CapeFeatureRenderer:hasCustomModelForLayer */ ,
			// /* ElytraFeatureRenderer:render */ ,
			// /* HorseArmorFeatureRenderer:render */ ,
			// /* LlamaDecorFeatureRenderer:render */ ,
			// /* WolfArmorFeatureREnderer:render */ ,
		}
	)
	private Object GetVariantEquippable(Object originalObj, @Local(argsOnly=true) ItemStack stack){
		return VariantsCitMod.EQUIPABLES.FeatureRendererMixin(originalObj, stack);
	}

}
