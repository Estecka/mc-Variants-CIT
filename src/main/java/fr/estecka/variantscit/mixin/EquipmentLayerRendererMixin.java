package fr.estecka.variantscit.mixin;

import java.util.function.BiFunction;
import java.util.function.Function;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import fr.estecka.variantscit.hooks.MixinGlobals;
import fr.estecka.variantscit.hooks.TrimPatternOverlay;
import fr.estecka.variantscit.VariantsCitMod;
import fr.estecka.variantscit.reload.EModuleHook;
import net.minecraft.util.Util;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer.TrimTextureKey;
import net.minecraft.client.resources.model.EquipmentAssetManager;
import net.minecraft.client.resources.palette.PalettedTextureManager;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;


@Unique
@Mixin(EquipmentLayerRenderer.class)
public class EquipmentLayerRendererMixin
{
	@Shadow private @Final Function<TrimTextureKey, PalettedTextureManager.Handle> trimTextureLookup;
	@Unique private BiFunction<TrimTextureKey, TrimPatternOverlay, PalettedTextureManager.Handle> trimSpriteOverlayLookup;

	@Inject(method="<init>", at=@At("TAIL"))
	private void getAtlas(EquipmentAssetManager manager, PalettedTextureManager atlas, CallbackInfo ci){
		this.trimSpriteOverlayLookup = Util.memoize(
			(trimSpriteKey, override) -> ComputeSprite(atlas, trimSpriteKey, override)
		);
	}

	@WrapOperation(
		method = "renderLayers(Lnet/minecraft/client/resources/model/EquipmentClientInfo$LayerType;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lnet/minecraft/world/item/ItemStack;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/resources/Identifier;II)V",
		at = @At(
			ordinal = 1,
			value = "INVOKE",
			target = "java/util/function/Function.apply(Ljava/lang/Object;)Ljava/lang/Object;"
		)
	)
	private Object GetTrimSprite(
		Function<?,?> memoizer,
		Object memoizerKey,
		Operation<Object> original,
		@Local(argsOnly=true) ItemStack stack
	){
		if (memoizer != this.trimTextureLookup || !(memoizerKey instanceof TrimTextureKey trimSpriteKey))
			throw new RuntimeException("Bad mixin injection point for variants-cit's trim_pattern hook.");

		Identifier overlayId = VariantsCitMod.GetModules().GetModelForItem(EModuleHook.TRIM_PATTERN, stack);
		TrimPatternOverlay trimOverlay = TrimPatternOverlay.REPOSITORY.Get(overlayId);

		if (trimOverlay == null)
			return original.call(memoizer, memoizerKey);
		else 
			return trimSpriteOverlayLookup.apply(trimSpriteKey, trimOverlay);

	}

	static private PalettedTextureManager.Handle ComputeSprite(PalettedTextureManager atlas, TrimTextureKey trimSpriteKey, TrimPatternOverlay overlay){
		try {
			MixinGlobals.trimOverride = overlay;
			return trimSpriteKey.getOrPrepareTexture(atlas);
		}
		finally {
			MixinGlobals.trimOverride = null;
		}
	}

	@Mixin(TrimTextureKey.class)
	static public abstract class ArmorTrimMixin
	{
		@ModifyExpressionValue(
			method = "getOrPrepareTexture",
			at = @At(
				value = "INVOKE",
				target = "net/minecraft/world/item/equipment/trim/TrimPattern.assetId()Lnet/minecraft/resources/Identifier;"
			)
		)
		public Identifier overrideTextureId(Identifier original){
			if (MixinGlobals.trimOverride != null)
				return MixinGlobals.trimOverride.assetId();
			else
				return original;
		}
	}
}
