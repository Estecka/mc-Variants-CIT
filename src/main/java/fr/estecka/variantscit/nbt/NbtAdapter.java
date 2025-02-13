package fr.estecka.variantscit.nbt;

import org.jetbrains.annotations.Nullable;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.AbstractNbtNumber;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtString;
import net.minecraft.util.StringIdentifiable;

public class NbtAdapter
{
	static public final Codec<NbtAdapter> CODEC = Codec.withAlternative(
		RecordCodecBuilder.create(builder->builder
			.group(
				NbtPath.CODEC.fieldOf("nbtPath").forGetter(adp -> adp.nbtPath),
				EType.CODEC.fieldOf("type").forGetter(adp -> adp.type)
			)
			.apply(builder, NbtAdapter::new)
		),
		NbtPath.CODEC.xmap(path->new NbtAdapter(path, EType.LITERAL), adp->adp.nbtPath)
	);

	public final EType type;
	private final String[] nbtPath;


	protected NbtAdapter(String[] nbtPath, EType type){
		this.nbtPath = nbtPath;
		this.type = type;
	}

	public final @Nullable String ResolveData(NbtElement nbt){
		nbt = NbtPath.Resolve(nbt, nbtPath);
		if (nbt == null)
			return null;
		else 
			return this.type.Adapt(nbt);
	}

	public enum EType
	implements StringIdentifiable
	{
		LITERAL("literal"){
			public String Adapt(NbtElement nbt){
				if (nbt instanceof NbtString)
					return nbt.asString();
				else if (nbt instanceof AbstractNbtNumber number)
					return number.numberValue().toString();
				else
					return null;
			}
		},
	
		// ID("identifier"),
		// ID_PATH("identifier_path"),
		// ID_NAMESPACE("identifier_namespace"),
		// NUMBER("number"),
		;
	
		static public final Codec<EType> CODEC = StringIdentifiable.createCodec(EType::values);
	
		private final String name;
	
		private EType(String name){
			this.name = name;
		}
	
		public abstract String Adapt(NbtElement nbt);
	
		@Override public String asString(){
			return this.name;
		}
	
	}
}
