package studio.mevera.scofi.animation.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Represents a sequence of change phases for an animation.
 * <p>
 * Caches the change phases and provides iteration and indexed access. Can be constructed from arrays or collections.
 * Used by {@link ChangeSequenceController} and {@link Animation} to manage animation frames.
 * </p>
 * <p>
 * Usage example:
 * <pre>
 *     ChangesSequence<String> sequence = ChangesSequence.of("Hello", "Welcome!");
 *     for (String change : sequence) {
 *         // your code here
 *     }
 * </pre>
 *
 * @param <T> the type to be changed
 * @since 1.0
 * @author Mqzen (aka Mqzn)
 */
public class ChangesSequence<T> implements Iterable<T> {

	/** List of change phases. */
	private final List<T> changes = new ArrayList<>();

	/**
	 * Constructs a sequence from an array of changes.
	 * @param changes array of changes
	 */
	@SafeVarargs
	ChangesSequence(T... changes) {
		this.changes.addAll(Arrays.asList(changes));
	}

	/**
	 * Constructs a sequence from a collection of changes.
	 * @param changes collection of changes
	 */
	ChangesSequence(Collection<T> changes) {
		this.changes.addAll(changes);
	}

	/**
	 * Creates a sequence from an array of changes.
	 * @param changes array of changes
	 * @return new ChangesSequence instance
	 */
	@SafeVarargs
	public static <T> ChangesSequence<T> of(T... changes) {
		return new ChangesSequence<>(changes);
	}

	/**
	 * Creates a sequence from a collection of changes.
	 * @param changes collection of changes
	 * @return new ChangesSequence instance
	 */
	public static <T> ChangesSequence<T> of(Collection<T> changes) {
		return new ChangesSequence<>(changes);
	}

	/**
	 * Returns an iterator over the change phases.
	 * @return iterator
	 */
	@NotNull
	@Override
	public Iterator<T> iterator() {
		return changes.iterator();
	}

	/**
	 * Gets a change by index, or null if out of bounds.
	 * @param index the index
	 * @return change at the index, or null
	 */
	public @Nullable T getChange(int index) {
		if(index >= changes.size() || index < 0) {
			return null;
		}
		return changes.get(index);
	}

	/**
	 * Adds a change to the sequence.
	 * @param change the change to add
	 */
	public void add(T change) {
		changes.add(change);
	}

	/**
	 * Gets the length of the sequence.
	 * @return number of changes
	 */
	int length() {
		return changes.size();
	}

}
