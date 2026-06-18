package fr.estecka.variantscit.modules.libraries;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import fr.estecka.variantscit.VariantsCitMod;
import fr.estecka.variantscit.commands.CommandLogger;
import fr.estecka.variantscit.commands.WalktroughLogger;

public class VariantLibrary
implements IVariantLibrary, IDebuggableLibrary<IVariantLibrary>
{
	protected final Identifier fallbackModel;
	protected final Map<Identifier,Identifier> variantModels;

	public VariantLibrary(Map<Identifier,Identifier> models){
		this.fallbackModel = models.get(FALLBACK_VARIANT_ID);
		this.variantModels = models;
	}
	public VariantLibrary(Identifier fallbackModels){
		this.fallbackModel = fallbackModels;
		this.variantModels = new HashMap<>();
	}

	public Identifier fallbackModel() { return this.fallbackModel; }
	public Map<Identifier,Identifier> variantModels() { return this.variantModels; }

	@Override
	public boolean HasVariantModel(Identifier variantId){
		return this.variantModels.containsKey(variantId);
	}

	@Override
	public @Nullable Identifier GetVariantModel(Identifier variantId){
		if (variantId == null)
			return null;
		else
			return this.variantModels.getOrDefault(variantId, this.fallbackModel);
	}

	@Override
	public @Nullable Identifier GetVariantModelStrict(Identifier variantId){
		return this.variantModels.get(variantId);
	}

	public int GetVariantCount(){
		return this.variantModels.size();
	}

	public boolean isEmpty(){
		return this.variantModels.isEmpty();
	}

	public VariantLibrary GetSubLibrary(String subPrefix){
		Map<Identifier,Identifier> subVariants = new HashMap<>();
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
		else for (var entry : this.SortedEntries())
		{
			logger.Info("{} -> {} ",
				CommandLogger.ItemData(entry.getKey()),
				CommandLogger.PackData(entry.getValue())
			);
		}
	}

	private Iterable<Map.Entry<Identifier,Identifier>> SortedEntries(){
		return this.variantModels.entrySet()
			.stream()
			.sorted((a,b)->{
				int r = a.getKey().getNamespace().compareTo(b.getKey().getNamespace());
				if (r == 0)
					r = a.getKey().getPath().compareTo(b.getKey().getPath());
				return r;
			})
			.toList()
			;
	}

	@Override
	public Snitch<IVariantLibrary> CreateSnitch(WalktroughLogger logger) {
		return new SnitchingLibrary(logger);
	}

	private class SnitchingLibrary
	extends IDebuggableLibrary.Snitch<IVariantLibrary>
	implements IVariantLibrary
	{
		public SnitchingLibrary (WalktroughLogger logger){
			super(logger);
		}

		@Override
		public boolean HasVariantModel(@Nullable Identifier variantId) {
			boolean r = VariantLibrary.this.HasVariantModel(variantId);
			this.OnTriedVariant(variantId, r);
			return r;
		}

		@Override
		public @Nullable Identifier GetVariantModel(Identifier variantId) {
			this.HasVariantModel(variantId);
			return VariantLibrary.this.GetVariantModel(variantId);
		}

		@Override
		public @Nullable Identifier GetVariantModelStrict(Identifier variantId) {
			this.HasVariantModel(variantId);
			return VariantLibrary.this.GetVariantModelStrict(variantId);
		}

		@Override
		protected void OnTriedVariant(Identifier variantId, boolean exists) {
			logger.Info("Tested variant ID: {}", CommandLogger.ItemData(variantId));
			super.OnTriedVariant(variantId, exists);
		}
	}

}
