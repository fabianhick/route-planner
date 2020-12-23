import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import org.jetbrains.annotations.NotNull;


public class FibonacciHeap<T> implements DataHeap<T> {


	public static final class FibonacciEntry<T> {

		private int degree = 0;
		private boolean marked = false;

		private FibonacciEntry<T> next;
		private FibonacciEntry<T> previous;

		private FibonacciEntry<T> parent;

		private FibonacciEntry<T> child;

		private T element;
		private double priority;

		public T getElement() {
			return this.element;
		}

		public void setElement(T element) {
			this.element = element;
		}

		public double getPriority() {
			return this.priority;
		}

		private FibonacciEntry(T element, double priority) {
			this.next = this.previous = this;
			this.element = element;
			this.priority = priority;
		}
	}

	private FibonacciEntry<T> min = null;

	private int size = 0;

	@Override
	public FibonacciEntry<T> insert(T key, final double priority) {
		this.checkPriority(priority);
		FibonacciEntry<T> result = new FibonacciEntry<T>(key, priority);

		this.min = merge(this.min, result);

		this.size++;

		return result;
	}

	@Override
	public T peek() {
		if (isEmpty()) {
			throw new NoSuchElementException("Heap is empty");
		}
		return this.min.element;
	}
	
	@Override
	public boolean isEmpty() {
		return this.min == null;
	}

	@Override
	public T poll() {
		if (isEmpty()) {
			throw new NoSuchElementException("Heap is empty");
		}
		
		size--;
		
		FibonacciEntry<T> min = this.min;
		
		if (this.min.next == this.min) {
			this.min = null;
		} else {
			this.min.previous.next = this.min.next;
			this.min.next.previous = this.min.previous;
			this.min = this.min.next;
		}
		
		if (min.child != null) {
			FibonacciEntry<?> current = min.child;
			do {
				current.parent = null;
				
				current = current.next;
			} while (current != min.child);
		}
		
		this.min = merge(this.min, min.child);
		
		if (this.min == null) {
			return min.element;
		}
		
		List<FibonacciEntry<T>> treeTable = new ArrayList<>();
		
		List<FibonacciEntry<T>> toVisit = new ArrayList<>();
		
		for (FibonacciEntry<T> current = this.min; toVisit.isEmpty() || toVisit.get(0) != current; current = current.next) {
			toVisit.add(current);
		}
		
		for (FibonacciEntry<T> current : toVisit) {
			while (true) {
				while (current.degree >= treeTable.size()) {
					treeTable.add(null);
				}
				
				if (treeTable.get(current.degree) == null) {
					treeTable.set(current.degree, current);
					break;
				}
				
				FibonacciEntry<T> other = treeTable.get(current.degree);
				treeTable.set(current.degree, null);
				
				FibonacciEntry<T> currentMin = (other.priority < current.priority) ? other : current;
				FibonacciEntry<T> currentMax = (other.priority < current.priority) ? current : other;
				
				currentMax.next.previous = currentMax.previous;
				currentMax.previous.next = currentMax.next;
				
				currentMax.next = currentMax.previous = currentMax;
				currentMin.child = merge(currentMin.child, currentMax);
				
				currentMax.parent = currentMin;
				
				currentMax.marked = false;
				
				currentMin.degree++;
				
				current = currentMin;
			}
			
			if (current.priority <= this.min.priority) {
				this.min = current;
			}
		}
		return min.element;
	}

	@Override
	public void decreaseKey(FibonacciEntry<T> key, double newPriority) {
		this.checkPriority(newPriority);
		if (newPriority > key.priority) {
			throw new IllegalArgumentException("New priority is too high");
		}

		key.priority = newPriority;

		if (key.parent != null && key.priority <= key.parent.priority) {
			cutNode(key);
		}

		if (key.priority <= this.min.priority) {
			this.min = key;
		}
	}

	private void cutNode(FibonacciEntry<T> key) {
		key.marked = false;
		if (key.parent == null) {
			return;
		}
		if (key.next != key) {
			key.next.previous =  key.previous;
			key.previous.next = key.next;
		}

		if (key.parent.child == key) {
			if (key.next != key) {
				key.parent.child = key.next;
			} else {
				key.parent.child = null;
			}
		}

		key.parent.degree--;

		key.previous = key.next = key;
		this.min = merge(this.min, key);

		if (key.parent.marked) {
			cutNode(key.parent);
		} else {
			key.parent.marked = true;
		}
		key.parent = null;
	}
	
	private void checkPriority(final double priority) {
		if (Double.isNaN(priority)) {
			throw new IllegalArgumentException("The given Priority " + priority + " is invalid");
		}
	}

	private static <T> FibonacciEntry<T> merge(final FibonacciEntry<T> first, final FibonacciEntry<T> second) {
		if (first == null && second == null) {
			return null;
		} else if (first != null && second == null) {
			return first;
		} else if (first == null && second != null) {
			return second;
		} else {
			FibonacciEntry<T> firstNext = first.next;
			first.next = second.next;
			first.next.previous = first;
			second.next = firstNext;
			second.next.previous = second;

			return first.priority < second.priority ? first : second;
		}
	}
}
