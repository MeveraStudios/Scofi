package studio.mevera.scofi.animation.core;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

/**
 * Controls the sequence of changes for an animation phase.
 * <p>
 * Manages the current index and provides methods to fetch the next and previous change in the sequence.
 * Used internally by {@link Animation} to iterate through change phases.
 * </p>
 * <p>
 * Usage example:
 * <pre>
 *     ChangeSequenceController<String> controller = ChangeSequenceController.newController(sequence);
 *     String next = controller.next();
 * </pre>
 *
 * @param <T> the type to be changed
 * @since 1.0
 * @author Mqzen (aka Mqzn)
 */

public class ChangeSequenceController<T> {

	/** Current index in the change sequence. */
	private @Getter int changeIndex = 0;
	/** The sequence of changes. */
	private final ChangesSequence<T> sequence;

	/**
	 * Constructs a controller for the given change sequence.
	 * @param sequence the sequence of changes
	 */
	protected ChangeSequenceController(ChangesSequence<T> sequence) {
		this.sequence = sequence;
	}

	/**
	 * Creates a new controller for the given change sequence.
	 * @param sequence the sequence of changes
	 * @return new ChangeSequenceController instance
	 */
	public static <T> ChangeSequenceController<T> newController(ChangesSequence<T> sequence){
		return new ChangeSequenceController<>(sequence);
	}

	/**
	 * Gets the next change in the sequence, cycling to the start if at the end.
	 * @return the next change
	 */
	public @NotNull T next()  {
		if(changeIndex >= sequence.length()) {
			changeIndex = 0;
		}
		T change = sequence.getChange(changeIndex);
		assert change != null;
		changeIndex++;
		return change;
	}

	/**
	 * Gets the previous change in the sequence, cycling to the end if at the start.
	 * @return the previous change
	 */
	public @NotNull T previous() {
		changeIndex--;
		if(changeIndex < 0) {
			changeIndex = sequence.length()-1;
		}
		T change = sequence.getChange(changeIndex);
		assert change != null;
		return change;
	}

}
