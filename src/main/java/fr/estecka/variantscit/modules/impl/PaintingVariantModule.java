package fr.estecka.variantscit.modules.impl;

import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.decoration.painting.PaintingVariant;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import fr.estecka.variantscit.itemdata.extractors.impl.PaintingVariantProperty;
import fr.estecka.variantscit.modules.cache.ECachePolicy;
import fr.estecka.variantscit.modules.libraries.IVariantLibrary;

public class PaintingVariantModule
extends AMonoComponentModule<CustomData>
{
	static public final PaintingVariantModule UNIT = new PaintingVariantModule();

	static public final Identifier INVALID_PAINTING = IVariantLibrary.SpecialVariantId("invalid");

	public PaintingVariantModule(){
		super(DataComponents.ENTITY_DATA, ECachePolicy.ALWAYS);
	}

	static public Optional<Registry<PaintingVariant>> GetPaintingRegistry(){
		Level world = Minecraft.getInstance().level;
		if (world != null)
			return world.registryAccess().lookup(Registries.PAINTING_VARIANT);
		else
			return Optional.empty();
	}

	public Identifier GetModelForComponent(CustomData component, IVariantLibrary models){
		if (component == null)
			return null;

		String rawVariant = PaintingVariantProperty.UNIT.GetPropertyValue(component);
		if (rawVariant == null)
			return null;

		Identifier variantId = Identifier.tryParse(rawVariant);
		if (variantId == null)
			return models.GetVariantModelStrict(INVALID_PAINTING);

		var registry = GetPaintingRegistry();
		if (registry.isPresent() && !registry.get().containsKey(variantId))
			return models.GetVariantModelStrict(INVALID_PAINTING);

		return models.GetVariantModel(variantId);
	}

}
