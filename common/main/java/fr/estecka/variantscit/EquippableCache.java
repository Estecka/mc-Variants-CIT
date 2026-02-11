package fr.estecka.variantscit;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import fr.estecka.variantscit.modules.IBakedModule;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.EquippableComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.equipment.EquipmentAssetKeys;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

public class EquippableCache
{
	/**
	 * Key 1: Maps original components to a list of their known variations.
	 * Key 2: Maps variant ID to the modified copy of the component.
	 */
	private final WeakHashMap<EquippableComponent, Map<Identifier, EquippableComponent>> cache = new WeakHashMap<>();

	public void Clear(){
		this.cache.clear();
	}

	public EquippableComponent GetWithAssetId(EquippableComponent original, Identifier id){
		return this.cache
			.computeIfAbsent(original, _0->new HashMap<>())
			.computeIfAbsent(id, _0->CopyWithAssetId(original, id))
			;
	}

	static private EquippableComponent CopyWithAssetId(EquippableComponent original, Identifier id){
		return new EquippableComponent(
			original.slot(),
			original.equipSound(),
			Optional.of(RegistryKey.of(EquipmentAssetKeys.REGISTRY_KEY, id)),
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
	public Object GetEquipableVariant(ItemStack stack, ComponentType<?> type, Operation<?> originalOp){
		Object original = originalOp.call(stack, type);

		if (type != DataComponentTypes.EQUIPPABLE
		|| !(original instanceof EquippableComponent equipable) )
		{
			return original;
		}

		final IBakedModule module = VariantsCitMod.GetEquipmentModule(stack.getItem());
		Identifier assetId = null;

		if (module != null){
			VariantsCitMod.LOGGER.PushLabel(stack.getItem());
			assetId = module.GetModelForItem(stack);
			VariantsCitMod.LOGGER.PopLabel();
		}

		if (assetId == null)
			return original;
		else
			return GetWithAssetId(equipable, assetId);
	}
}
