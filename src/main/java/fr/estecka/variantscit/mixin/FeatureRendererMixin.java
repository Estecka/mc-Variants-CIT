package fr.estecka.variantscit.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import fr.estecka.variantscit.VariantsCitMod;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.feature.CapeFeatureRenderer;
import net.minecraft.client.render.entity.feature.ElytraFeatureRenderer;
import net.minecraft.client.render.entity.feature.HorseArmorFeatureRenderer;
import net.minecraft.client.render.entity.feature.LlamaDecorFeatureRenderer;
import net.minecraft.client.render.entity.feature.WolfArmorFeatureRenderer;
import net.minecraft.component.ComponentType;
import net.minecraft.item.ItemStack;

public class FeatureRendererMixin
{
	@Mixin(ArmorFeatureRenderer.class)
	static public class Armor
	{
		@WrapOperation(
			method = { "renderArmor", "hasModel" },
			at=@At(value="INVOKE", ordinal=0, target="net/minecraft/item/ItemStack.get(Lnet/minecraft/component/ComponentType;)Ljava/lang/Object;")
		)
		static private Object GetVariantEquippable(ItemStack stack, ComponentType<?> type, Operation<?> originalOp){
			return VariantsCitMod.EQUIPABLES.GetEquipableVariant(stack, type, originalOp);
		}
	}

	@Mixin(CapeFeatureRenderer.class)
	static public class Cape
	{
		@WrapOperation(
			method = { "hasCustomModelForLayer" },
			at=@At(value="INVOKE", ordinal=0, target="net/minecraft/item/ItemStack.get(Lnet/minecraft/component/ComponentType;)Ljava/lang/Object;")
		)
		static private Object GetVariantEquippable(ItemStack stack, ComponentType<?> type, Operation<?> originalOp){
			return VariantsCitMod.EQUIPABLES.GetEquipableVariant(stack, type, originalOp);
		}
	}

	@Mixin(ElytraFeatureRenderer.class)
	static public class Elytra
	{
		@WrapOperation(
			method = { "render" },
			at=@At(value="INVOKE", ordinal=0, target="net/minecraft/item/ItemStack.get(Lnet/minecraft/component/ComponentType;)Ljava/lang/Object;")
		)
		static private Object GetVariantEquippable(ItemStack stack, ComponentType<?> type, Operation<?> originalOp){
			return VariantsCitMod.EQUIPABLES.GetEquipableVariant(stack, type, originalOp);
		}
	}

	@Mixin(HorseArmorFeatureRenderer.class)
	static public class Horse
	{
		@WrapOperation(
			method = { "render" },
			at=@At(value="INVOKE", ordinal=0, target="net/minecraft/item/ItemStack.get(Lnet/minecraft/component/ComponentType;)Ljava/lang/Object;")
		)
		static private Object GetVariantEquippable(ItemStack stack, ComponentType<?> type, Operation<?> originalOp){
			return VariantsCitMod.EQUIPABLES.GetEquipableVariant(stack, type, originalOp);
		}
	}

	@Mixin(LlamaDecorFeatureRenderer.class)
	static public class Llama
	{
		@WrapOperation(
			method = { "render" },
			at=@At(value="INVOKE", ordinal=0, target="net/minecraft/item/ItemStack.get(Lnet/minecraft/component/ComponentType;)Ljava/lang/Object;")
		)
		static private Object GetVariantEquippable(ItemStack stack, ComponentType<?> type, Operation<?> originalOp){
			return VariantsCitMod.EQUIPABLES.GetEquipableVariant(stack, type, originalOp);
		}
	}

	@Mixin(WolfArmorFeatureRenderer.class)
	static public class Wolf
	{
		@WrapOperation(
			method = { "render" },
			at=@At(value="INVOKE", ordinal=0, target="net/minecraft/item/ItemStack.get(Lnet/minecraft/component/ComponentType;)Ljava/lang/Object;")
		)
		static private Object GetVariantEquippable(ItemStack stack, ComponentType<?> type, Operation<?> originalOp){
			return VariantsCitMod.EQUIPABLES.GetEquipableVariant(stack, type, originalOp);
		}
	}
}
