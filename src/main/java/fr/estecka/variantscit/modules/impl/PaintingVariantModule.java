package fr.estecka.variantscit.modules.impl;

import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import fr.estecka.variantscit.api.IVariantManager;
import fr.estecka.variantscit.format.properties.PaintingVariantProperty;

/**
 * @implNote
 * Because of the "invalid" special model, this particular module's cache should
 * not be allowed to survive  **datapack**  reloads. However this is unlikely to
 * cause  any issue: so long as  an item stack's lifetime  is tied to its world,
 * then the cached keys  will not survive that world, and thus won't survive the
 * actual reloading of painting variants.
 */
public class PaintingVariantModule
extends AComponentCachingModule<CustomData>
{
	public PaintingVariantModule(){
		super(DataComponents.ENTITY_DATA);
	}

	static public Optional<Registry<PaintingVariant>> GetPaintingRegistry(){
		@SuppressWarnings("resource")
		Level world = Minecraft.getInstance().level;
		if (world != null)
			return world.registryAccess().lookup(Registries.PAINTING_VARIANT);
		else
			return Optional.empty();
	}

	public ResourceLocation GetModelForComponent(CustomData component, IVariantManager models){
		if (component == null)
			return null;

		String rawVariant = PaintingVariantProperty.UNIT.GetPropertyString(component);
		if (rawVariant == null)
			return null;

		ResourceLocation variantId = ResourceLocation.tryParse(rawVariant);
		if (variantId == null)
			return models.GetSpecialModel("invalid");

		var registry = GetPaintingRegistry();
		if (registry.isPresent() && !registry.get().containsKey(variantId))
			return models.GetSpecialModel("invalid");

		return models.GetVariantModel(variantId);
	}

}
