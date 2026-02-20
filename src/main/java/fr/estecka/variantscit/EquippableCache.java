package fr.estecka.variantscit;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.item.equipment.Equippable;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import fr.estecka.variantscit.modules.IBakedModule;
import fr.estecka.variantscit.reload.EModuleContext;

public class EquippableCache
{
	/**
	 * Key 1: Maps original components to a list of their known variations.
	 * Key 2: Maps variant ID to the modified copy of the component.
	 */
	private final WeakHashMap<Equippable, Map<ResourceLocation, Equippable>> cache = new WeakHashMap<>();

	public void Clear(){
		this.cache.clear();
	}

	public Equippable GetWithAssetId(Equippable original, ResourceLocation id){
		return this.cache
			.computeIfAbsent(original, _0->new HashMap<>())
			.computeIfAbsent(id, _0->CopyWithAssetId(original, id))
			;
	}

	static private Equippable CopyWithAssetId(Equippable original, ResourceLocation id){
		return new Equippable(
			original.slot(),
			original.equipSound(),
			Optional.of(ResourceKey.create(EquipmentAssets.ROOT_ID, id)),
			original.cameraOverlay(),
			original.allowedEntities(),
			original.dispensable(),
			original.swappable(),
			original.damageOnHurt()
		);
	}

	/**
	 * Mixin injection.
	 * See: {@link fr.estecka.variantscit.mixin.FeatureRendererMixins}
	 */
	public Object GetEquipableVariant(ItemStack stack, DataComponentType<?> type, Operation<?> originalOp){
		Object original = originalOp.call(stack, type);

		if (type != DataComponents.EQUIPPABLE
		|| !(original instanceof Equippable equipable) )
		{
			return original;
		}

		VariantsCitMod.LOGGER.PushLabel(stack.getItem());
		ResourceLocation assetId = VariantsCitMod.GetModules().GetModelForItem(EModuleContext.EQUIPPABLE, stack);
		VariantsCitMod.LOGGER.PopLabel();

		if (assetId == null)
			return original;
		else
			return GetWithAssetId(equipable, assetId);
	}
}
