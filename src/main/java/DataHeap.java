import org.jetbrains.annotations.NotNull;


public interface DataHeap<T> {

	public FibonacciHeap.FibonacciEntry<T> insert(final T key, final double priority);

	public T peek();

	public T poll();

	public void decreaseKey(final FibonacciHeap.FibonacciEntry<T> key, double newPriority);
	
	public boolean isEmpty();
}
