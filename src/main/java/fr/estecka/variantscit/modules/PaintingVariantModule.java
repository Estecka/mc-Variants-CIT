package fr.estecka.variantscit.modules;

import java.util.Optional;
import fr.estecka.variantscit.api.IVariantManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.decoration.painting.PaintingVariant;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

/**
 * @implNote
 * Because of the "invalid" special model, this particular module's cache should
 * not be allowed to survive  **datapack**  reloads. However this is unlikely to
 * cause  any issue: so long as  an item stack's lifetime  is tied to its world,
 * then the cached keys  will not survive that world, and thus won't survive the
 * actual reloading of painting variants.
 */
public class PaintingVariantModule
extends AComponentCachingModule<NbtComponent>
{
	public PaintingVariantModule(){
		super(DataComponentTypes.ENTITY_DATA);
	}

	static public Optional<Registry<PaintingVariant>> GetPaintingRegistry(){
		@SuppressWarnings("resource")
		World world = MinecraftClient.getInstance().world;
		if (world != null)
			return world.getRegistryManager().getOptional(RegistryKeys.PAINTING_VARIANT);
		else
			return Optional.empty();
	}

	public ModelIdentifier GetModelForComponent(NbtComponent component, IVariantManager models){
		if (component == null)
			return null;

		String rawVariant = component.getNbt().getString("variant");
		if (rawVariant == null)
			return null;

		Identifier variantId = Identifier.tryParse(rawVariant);
		if (variantId == null)
			return models.GetSpecialModel("invalid");

		var registry = GetPaintingRegistry();
		if (registry.isPresent() && !registry.get().containsId(variantId))
			return models.GetSpecialModel("invalid");

		return models.GetVariantModel(variantId);
	}

}
