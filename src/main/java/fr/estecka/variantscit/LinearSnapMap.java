package fr.estecka.variantscit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.function.Predicate;

/**
 * Sort entries like points on a line. When a position  is requested, it returns
 * the closest entry on a given side of that position.
 */
public class LinearSnapMap<T>
{
	static public record Entry<T>(int magnitude, T value){}

	private final ArrayList<Entry<T>> entries = new ArrayList<>();
	private final Comparator<T> discriminator;
	
	/**
	 * @param discriminator Will be used to sort values of equal magnitude.
	 */
	public LinearSnapMap(Comparator<T> discriminator){
		this.discriminator = discriminator;
	}

	public LinearSnapMap(){
		this((a,b)->0);
	}


	private int CompareEntries(Entry<T> a, Entry<T> b){
		int r = Integer.compare(a.magnitude, b.magnitude);
		if (r != 0)
			return r;
		else
			return this.discriminator.compare(a.value, b.value);
	}

	public final void AddEntry(int magnitude, T value){
		this.entries.add(new Entry<T>(magnitude, value));
		this.entries.sort(this::CompareEntries);
	}

	public final void AddEntries(Collection<Entry<T>> entries){
		this.entries.addAll(entries);
		this.entries.sort(this::CompareEntries);
	}

	/**
	 * Binary  search  through  the array  to find  the entry  with  the closest
	 * magnitude. More specifically, this searches for  the elligible entry that
	 * separates all elligible entries from the inelligible ones.
	 * 
	 * @param bias -1, 0, or +1. Defines which range of entries are elligible:
	 * Those that are greater or equal (+1), lesser or equal (-1) or both (0).
	 * If multiple entries have the same magnitude:
	 * - For lesser magnitudes, pick the highest index
	 * - For greater magnitudes, pick the lowest index
	 * - For equal magnitudes, pick depending on the bias.
	 * - For equal magnitudes with a bias of 0, immediately returns whichever is
	 *   found first.
	 * @return The index  of the  delimiting entry, or -1 if  no elligible entry
	 * was found.
	 */
	private int GetClosestIndex(int targetMagnitude, int bias){
		if (this.entries.size() < 1)
			return -1;

		int iMin = 0;
		int iMax = this.entries.size()-1;
		int magMin = this.entries.getFirst().magnitude;
		int magMax = this.entries.getLast ().magnitude;
		// All entries are outside the elligible range.
		if ((bias < 0 && magMin > targetMagnitude)
		||  (bias > 0 && magMax < targetMagnitude)
		) {
			return -1;
		}
		/**
		 * Past this point, it is guaranteed that one bound  always points to an
		 * elligible  entry, and the  other to an inelligible one. (Which one is
		 * which varies with the bias.)
		 */

		// Stop when the bounds are adjacent.
		while (iMin+1 < iMax){
			int midpoint = (iMax + iMin) / 2;
			Entry<T> midEntry = entries.get(midpoint);
			if (midEntry.magnitude == targetMagnitude && bias == 0)
				return midpoint;

			// Would cause an infinite loop.
			// Should never happen so long as the bounds are not adjacent.
			assert midpoint != iMin && midpoint != iMax;

			boolean nudgeUp;
			if (midEntry.magnitude < targetMagnitude)
				nudgeUp = true;
			else if (midEntry.magnitude > targetMagnitude)
				nudgeUp = false;
			else 
				nudgeUp = (bias < 0);

			if (nudgeUp){
				magMin = midEntry.magnitude;
				iMin = midpoint;
			}
			else {
				magMax = midEntry.magnitude;
				iMax = midpoint;
			}
		}

		if (bias > 0)
			return iMax;
		if (bias < 0)
			return iMin;
		// Returns closest
		return ((magMax+magMin)/2) > targetMagnitude ? iMin : iMax;
	}

	public final T GetClosestValue(int targetMagnitude, int bias){
		int i = GetClosestIndex(targetMagnitude, bias);
		if (i < 0)
			return null;
		else
			return this.entries.get(GetClosestIndex(targetMagnitude, bias)).value;
	}

	/**
	 * TODO: Behaviour for bias==0 is not implemented. There is currently no use
	 * case for this in the mod.
	 */
	public final T GetClosestValue(int targetMagnitude, int bias, Predicate<T> isElligible){
		int i = GetClosestIndex(targetMagnitude, bias);
		if (i < 0)
			return null;

		int direction = (bias>0) ? +1 : -1;
		for (; 0<=i && i<entries.size(); i+=direction){
			Entry<T> result = this.entries.get(i);
			if (isElligible.test(result.value))
				return result.value;
		}

		return null;
	}

}
