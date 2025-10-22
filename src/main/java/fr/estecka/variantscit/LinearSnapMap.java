package fr.estecka.variantscit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.function.Predicate;

/**
 * Sort entries as  points on a line. When a position is requested, it looks for
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
	 * TODO: Behaviour for bias==0 is not correctly implemented and not properly
	 * defined. There are currently no use cases for this in the mod.
	 * 
	 * @param bias -1, 0, or +1. Defines which range of entries are elligible:
	 * Those that are greater or equal (+1), lesser or equal (-1) or both (0).
	 * @implNote If multiple entries have the same magnitude:
	 * - For lesser magnitudes, pick the highest index
	 * - For greater magnitudes, pick the lowest index
	 * - For equal magnitudes, pick depending on the bias.
	 * - For equal magnitudes with a bias of 0, return immediately.
	 * @return The index  of the  delimiting entry, or -1 if  no elligible entry
	 * was found.
	 */
	private int GetClosestIndex(int targetMagnitude, int bias){
		int iMin = -1;
		int iMax = this.entries.size();
		int iBestFit = -1;

		assert bias != 0;

		// Stop when there are no more entries between the bounds (excluded).
		while (iMin + 1 < iMax){
			int midpoint = (iMax + iMin) / 2;
			int midMagnitude = entries.get(midpoint).magnitude;

			if (midMagnitude == targetMagnitude && bias == 0)
				return midpoint;

			if (midMagnitude == targetMagnitude
			|| (midMagnitude < targetMagnitude && bias < 0)
			|| (midMagnitude > targetMagnitude && bias > 0)
			){
				iBestFit = midpoint;
			}

			boolean nudgeUp;
			if (midMagnitude < targetMagnitude)
				nudgeUp = true;
			else if (midMagnitude > targetMagnitude)
				nudgeUp = false;
			else 
				nudgeUp = (bias < 0);

			if (nudgeUp){
				iMin = midpoint;
			}
			else {
				iMax = midpoint;
			}
		}

		return iBestFit;
	}

	/**
	 * Finds the entry  with the best fitting magnitude. Assumes all entries are
	 * elligible, and no ties can occur.
	 */
	public final T GetClosestValue(int targetMagnitude, int bias){
		int i = GetClosestIndex(targetMagnitude, bias);
		if (i < 0)
			return null;
		else
			return this.entries.get(GetClosestIndex(targetMagnitude, bias)).value;
	}

	/**
	 * Finds the entry  with the best fitting magnitude, and uses the comparator
	 * given to the constructor is used as tiebreaker.
	 * 
	 * @implNote This is more performant than using a custom comparator. Entries
	 * are sorted ahead of time, so the function  can return the first elligible
	 * entry it encounters.
	 * 
	 * TODO: Behaviour for bias==0 is not implemented.
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

	/**
	 * Finds  the entry  with  the best  possible magnitude, and uses  the given
	 * comparator as a tiebreaker.
	 * 
	 * TODO: Behaviour for bias==0 is not implemented.
	 */
	public final T GetClosestValue(int targetMagnitude, int bias, Predicate<T> isElligible, Comparator<T> comparator){
		int i = GetClosestIndex(targetMagnitude, bias);
		if (i < 0)
			return null;

		Entry<T> bestFit = null;

		int direction = (bias>0) ? +1 : -1;
		for (; 0<=i && i<entries.size(); i+=direction){
			Entry<T> result = this.entries.get(i);
			if (bestFit == null && isElligible.test(result.value))
				bestFit = result;
			else if (result.magnitude != bestFit.magnitude)
				break;
			else if (isElligible.test(result.value) && comparator.compare(bestFit.value, result.value) < 0)
				bestFit = result;
		}

		return bestFit.value;
	}

}
