package fr.estecka.variantscit;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.EquippableComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.equipment.EquipmentAssetKeys;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

public class EquippableCache
{
	// TODO: Cache may needs to be cleared from times to times, same as component-cahing modules.
	private final Map<EquippableComponent, Map<Identifier, EquippableComponent>> cache = new HashMap<>();

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

		final IItemModelProvider module = VariantsCitMod.GetEquipmentModule(stack.getItem());
		Identifier assetId;

		if (module == null || (assetId=module.GetModelForItem(stack)) == null)
			return original;
		else
			return GetWithAssetId(equipable, assetId);
	}
}
