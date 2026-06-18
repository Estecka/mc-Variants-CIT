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
import fr.estecka.variantscit.MixinGlobals;
import fr.estecka.variantscit.VariantsCitMod;
import fr.estecka.variantscit.reload.EModuleHook;
import fr.estecka.variantscit.trims.TrimPatternOverlay;
import net.minecraft.Util;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer.TrimSpriteKey;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.EquipmentAssetManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;


@Unique
@Mixin(EquipmentLayerRenderer.class)
public class EquipmentLayerRendererMixin
{
	@Shadow private @Final Function<TrimSpriteKey,TextureAtlasSprite> trimSpriteLookup;
	@Unique private BiFunction<TrimSpriteKey, TrimPatternOverlay, TextureAtlasSprite> trimSpriteOverlayLookup;

	@Inject(method="<init>", at=@At("TAIL"))
	private void getAtlas(EquipmentAssetManager manager, TextureAtlas atlas, CallbackInfo ci){
		this.trimSpriteOverlayLookup = Util.memoize(
			(trimSpriteKey, override) -> ComputeSprite(atlas, trimSpriteKey, override)
		);
	}

	@WrapOperation(
		method = "renderLayers(Lnet/minecraft/client/resources/model/EquipmentClientInfo$LayerType;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/client/model/Model;Lnet/minecraft/world/item/ItemStack;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/resources/ResourceLocation;)V",
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
		if (memoizer != this.trimSpriteLookup || !(memoizerKey instanceof TrimSpriteKey trimSpriteKey))
			throw new RuntimeException("Bad mixin injection point for variants-cit's trim_pattern hook.");

		ResourceLocation overlayId = VariantsCitMod.GetModules().GetModelForItem(EModuleHook.TRIM_PATTERN, stack);
		TrimPatternOverlay trimOverlay = TrimPatternOverlay.REPOSITORY.Get(overlayId);

		if (trimOverlay == null)
			return original.call(memoizer, memoizerKey);
		else 
			return trimSpriteOverlayLookup.apply(trimSpriteKey, trimOverlay);

	}

	static private TextureAtlasSprite ComputeSprite(TextureAtlas atlas, TrimSpriteKey trimSpriteKey, TrimPatternOverlay overlay){
		ResourceLocation spriteId;
		try {
			MixinGlobals.trimOverride = overlay;
			spriteId = trimSpriteKey.textureId();
		}
		finally {
			MixinGlobals.trimOverride = null;
		}

		return atlas.getSprite(spriteId);

	}

	@Mixin(TrimSpriteKey.class)
	static public abstract class TrimSpriteKeyMixin
	{
		@ModifyExpressionValue(
			method = "textureId",
			at = @At(
				value = "INVOKE",
				target = "net/minecraft/world/item/equipment/trim/TrimPattern.assetId()Lnet/minecraft/resources/ResourceLocation;"
			)
		)
		public ResourceLocation overrideTextureId(ResourceLocation original){
			if (MixinGlobals.trimOverride != null)
				return MixinGlobals.trimOverride.assetId();
			else
				return original;
		}
	}
}
