package fr.estecka.variantscit.itemdata.nbtpath;

import org.jetbrains.annotations.Nullable;
import net.minecraft.nbt.Tag;

interface Token {
	@Nullable Tag Resolve(Tag nbt);
}
