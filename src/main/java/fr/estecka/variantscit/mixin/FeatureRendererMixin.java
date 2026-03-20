package fr.estecka.variantscit.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import fr.estecka.variantscit.VariantsCitMod;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import net.minecraft.client.renderer.entity.layers.HorseArmorLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.LlamaDecorLayer;
import net.minecraft.client.renderer.entity.layers.WingsLayer;
import net.minecraft.client.renderer.entity.layers.WolfArmorLayer;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;

public class FeatureRendererMixin
{
	@Mixin(HumanoidArmorLayer.class)
	static public class Armor
	{
		@WrapOperation(
			method = { "renderArmorPiece", "shouldRender" },
			at=@At(value="INVOKE", ordinal=0, target="net/minecraft/world/item/ItemStack.get(Lnet/minecraft/core/component/DataComponentType;)Ljava/lang/Object;")
		)
		static private Object GetVariantEquippable(ItemStack stack, DataComponentType<?> type, Operation<?> originalOp){
			return VariantsCitMod.EQUIPABLES.GetEquipableVariant(stack, type, originalOp);
		}
	}

	@Mixin(CapeLayer.class)
	static public class Cape
	{
		@WrapOperation(
			method = { "hasLayer" },
			at=@At(value="INVOKE", ordinal=0, target="net/minecraft/world/item/ItemStack.get(Lnet/minecraft/core/component/DataComponentType;)Ljava/lang/Object;")
		)
		static private Object GetVariantEquippable(ItemStack stack, DataComponentType<?> type, Operation<?> originalOp){
			return VariantsCitMod.EQUIPABLES.GetEquipableVariant(stack, type, originalOp);
		}
	}

	@Mixin(WingsLayer.class)
	static public class Elytra
	{
		@WrapOperation(
			method = { "render" },
			at=@At(value="INVOKE", ordinal=0, target="net/minecraft/world/item/ItemStack.get(Lnet/minecraft/core/component/DataComponentType;)Ljava/lang/Object;")
		)
		static private Object GetVariantEquippable(ItemStack stack, DataComponentType<?> type, Operation<?> originalOp){
			return VariantsCitMod.EQUIPABLES.GetEquipableVariant(stack, type, originalOp);
		}
	}

	@Mixin(HorseArmorLayer.class)
	static public class Horse
	{
		@WrapOperation(
			method = { "render" },
			at=@At(value="INVOKE", ordinal=0, target="net/minecraft/world/item/ItemStack.get(Lnet/minecraft/core/component/DataComponentType;)Ljava/lang/Object;")
		)
		static private Object GetVariantEquippable(ItemStack stack, DataComponentType<?> type, Operation<?> originalOp){
			return VariantsCitMod.EQUIPABLES.GetEquipableVariant(stack, type, originalOp);
		}
	}

	@Mixin(LlamaDecorLayer.class)
	static public class Llama
	{
		@WrapOperation(
			method = { "render" },
			at=@At(value="INVOKE", ordinal=0, target="net/minecraft/world/item/ItemStack.get(Lnet/minecraft/core/component/DataComponentType;)Ljava/lang/Object;")
		)
		static private Object GetVariantEquippable(ItemStack stack, DataComponentType<?> type, Operation<?> originalOp){
			return VariantsCitMod.EQUIPABLES.GetEquipableVariant(stack, type, originalOp);
		}
	}

	@Mixin(WolfArmorLayer.class)
	static public class Wolf
	{
		@WrapOperation(
			method = { "render" },
			at=@At(value="INVOKE", ordinal=0, target="net/minecraft/world/item/ItemStack.get(Lnet/minecraft/core/component/DataComponentType;)Ljava/lang/Object;")
		)
		static private Object GetVariantEquippable(ItemStack stack, DataComponentType<?> type, Operation<?> originalOp){
			return VariantsCitMod.EQUIPABLES.GetEquipableVariant(stack, type, originalOp);
		}
	}
}
