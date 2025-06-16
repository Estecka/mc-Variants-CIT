package fr.estecka.variantscit;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.component.type.EquippableComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.equipment.EquipmentAssetKeys;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

public class EquippableCache
{
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
		var assetId = RegistryKey.of(EquipmentAssetKeys.REGISTRY_KEY, id);
		return new EquippableComponent(
			original.slot(),
			original.equipSound(),
			Optional.of(assetId),
			original.cameraOverlay(),
			original.allowedEntities(),
			original.dispensable(),
			original.swappable(),
			original.damageOnHurt()
		);
	}

	/**
	 * See: {@link fr.estecka.variantscit.mixin.FeatureRendererMixin}
	 */
	public Object FeatureRendererMixin(Object originalObj, ItemStack stack){
		if (!(originalObj instanceof EquippableComponent original))
			return originalObj;

		IItemModelProvider module = VariantsCitMod.GetEquipmentModule(stack.getItem());
		Identifier assetId;

		if (module == null || (assetId=module.GetModelForItem(stack)) == null)
			return original;
		else
			return GetWithAssetId(original, assetId);
	}
}
