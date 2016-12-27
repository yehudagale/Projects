import java.util.HashSet;
import java.util.LinkedList;
import java.util.Collection;
class MyLinkedHashSet<E> extends LinkedList<E>{
	private HashSet<E> set  = new HashSet<E>();
	@Override
	public boolean addAll(Collection<? extends E> c)
	{
		boolean changed = false;
		if (c == null) {
			return false;
		}
		for (E item : c) {
			if (this.add(item)) {
				changed = true;
			}
		}
		return changed;
	}
	@Override
	public boolean add(E item)
	{
		if (set.add(item)) {
			return super.add(item);
		}
		else{
			return false;
		}
	}
	@Override
	public E remove()
	{
		E toReturn = super.remove();
		set.remove(toReturn);
		return toReturn;
	}
	@Override
	public E poll()
	{
		E toReturn = super.poll();
		set.remove(toReturn);
		return toReturn;
	}

}