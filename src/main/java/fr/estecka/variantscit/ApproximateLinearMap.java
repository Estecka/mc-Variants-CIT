package fr.estecka.variantscit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.function.Predicate;

/**
 * Sort entries like points on a line. When a position is requested, it returns
 * the closest entry instead on a given side of that position.
 */
public class ApproximateLinearMap<T>
{
	static public record Entry<T>(int magnitude, T value){}

	private final ArrayList<Entry<T>> entries = new ArrayList<>();
	private final Comparator<T> discriminator;

	public ApproximateLinearMap(Comparator<T> discriminator){
		this.discriminator = discriminator;
	}

	public ApproximateLinearMap(){
		this.discriminator = (a,b)->0;
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
	 * @implNote Starts with a binary search to find the closest possible entry,
	 * then iterate starting from that point until an elligible entry is found.
	 * 
	 * @param targetMagnitude The optimal magnitude to return.
	 * @param greaterThan Whether to fallback to entries that are greater (true)
	 * or lower (false) than the target magnitude.
	 * @param isElligible Tests whether an entry is acceptable to return.
	 * @return
	 */
	public final T GetClosestValue(int targetMagnitude, boolean greaterThan, Predicate<Entry<T>> isElligible){
		int iTarget = 0;
		int iMin = 0;
		int iMax = this.entries.size();

		while (iMin < iMax){
			int halfPoint = (iMax - iMin) / 2;
			Entry<T> entry = entries.get(halfPoint);

			if (entry.magnitude < targetMagnitude)
				iMin = halfPoint;
			else if (entry.magnitude > targetMagnitude)
				iMax = halfPoint;
			else if (greaterThan)
				iMin = halfPoint;
			else
				iMax = halfPoint;

		}

		int direction = greaterThan ? +1 : -1;
		for (int i=iTarget; 0<=i && i<entries.size(); i+=direction){
			Entry<T> result = this.entries.get(i);
			if (isElligible.test(result))
				return result.value;
		}

		return null;
	}

	public final T GetClosestValue(int targetMagnitude, boolean greaterThan){
		return GetClosestValue(targetMagnitude, greaterThan, _0->true);
	}
}
