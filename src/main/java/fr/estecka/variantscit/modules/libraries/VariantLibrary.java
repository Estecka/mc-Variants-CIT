package fr.estecka.variantscit.modules.libraries;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import fr.estecka.variantscit.VariantsCitMod;
import fr.estecka.variantscit.commands.CommandLogger;

public class VariantLibrary
implements IVariantLibrary, IDebuggableLibrary<IVariantLibrary>
{
	protected final ResourceLocation fallbackModel;
	protected final Map<ResourceLocation,ResourceLocation> variantModels;

	public VariantLibrary(Map<ResourceLocation,ResourceLocation> models){
		this.fallbackModel = models.get(FALLBACK_VARIANT_ID);
		this.variantModels = models;
	}

	public ResourceLocation fallbackModel() { return this.fallbackModel; }
	public Map<ResourceLocation,ResourceLocation> variantModels() { return this.variantModels; }

	@Override
	public boolean HasVariantModel(ResourceLocation variantId){
		return this.variantModels.containsKey(variantId);
	}

	@Override
	public @Nullable ResourceLocation GetVariantModel(ResourceLocation variantId){
		if (variantId == null)
			return null;
		else
			return this.variantModels.getOrDefault(variantId, this.fallbackModel);
	}

	@Override
	public @Nullable ResourceLocation GetVariantModelStrict(ResourceLocation variantId){
		return this.variantModels.get(variantId);
	}

	public int GetVariantCount(){
		return this.variantModels.size();
	}

	public boolean isEmpty(){
		return this.variantModels.isEmpty();
	}

	public VariantLibrary GetSubLibrary(String subPrefix){
		Map<ResourceLocation,ResourceLocation> subVariants = new HashMap<>();
		for (var entry : this.variantModels.entrySet())
		{
			if (entry.getKey().getNamespace().equals(VariantsCitMod.MODID))
				subVariants.put(entry.getKey(), entry.getValue());
			else if (entry.getKey().getPath().startsWith(subPrefix))
			{
				subVariants.put(
					entry.getKey().withPath(path->path.substring(subPrefix.length())),
					entry.getValue()
				);
			}
		}

		return new VariantLibrary(Map.copyOf(subVariants));
	}


/******************************************************************************/
/* # Debug Commands                                                           */
/******************************************************************************/

	@Override
	public void Summary(CommandLogger logger){
		logger.Info("This module handles {} variants.", this.variantModels.size());
	}

	@Override
	public void Dump(CommandLogger logger){
		if (this.variantModels.isEmpty())
			logger.Info("This module does not have any variant.");
		else for (var entry : this.variantModels.entrySet())
		{
			logger.Info("{} -> {} ",
				CommandLogger.ItemData(entry.getKey()),
				CommandLogger.PackData(entry.getValue())
			);
		}
	}

	@Override
	public Snitch<IVariantLibrary> CreateSnitch(CommandLogger logger) {
		return new SnitchingLibrary(logger);
	}

	private class SnitchingLibrary
	extends IDebuggableLibrary.Snitch<IVariantLibrary>
	implements IVariantLibrary
	{
		public SnitchingLibrary (CommandLogger logger){
			super(logger);
		}

		@Override
		public boolean HasVariantModel(@Nullable ResourceLocation variantId) {
			boolean r = VariantLibrary.this.HasVariantModel(variantId);
			this.OnTriedVariant(variantId, r);
			return r;
		}

		@Override
		public @Nullable ResourceLocation GetVariantModel(ResourceLocation variantId) {
			this.HasVariantModel(variantId);
			return VariantLibrary.this.GetVariantModel(variantId);
		}

		@Override
		public @Nullable ResourceLocation GetVariantModelStrict(ResourceLocation variantId) {
			this.HasVariantModel(variantId);
			return VariantLibrary.this.GetVariantModelStrict(variantId);
		}

		@Override
		protected void OnTriedVariant(ResourceLocation variantId, boolean exists) {
			logger.Info("Tested variant ID: {}", CommandLogger.ItemData(variantId));
			super.OnTriedVariant(variantId, exists);
		}
	}

}
