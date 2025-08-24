package studio.mevera.scofi.animation.core;

import lombok.Getter;

/**
 * Represents a generic animation sequence for any type of object.
 * <p>
 * Caches the original value and manages a sequence of changes using {@link ChangeSequenceController}.
 * Provides methods to fetch the next and previous change, as well as the current index in the sequence.
 * </p>
 * <p>
 * Usage example:
 * <pre>
 *     Animation<String> anim = new Animation<>("Hello", "Welcome", "Goodbye");
 *     String frame = anim.fetchNextChange();
 * </pre>
 *
 * @param <T> the type of object being animated
 * @since 1.0
 * @author Mqzen (aka Mqzn)
 */
public class Animation<T>  {
	/** The original value of the animation. */
	protected final @Getter T original;
	/** Controller for the sequence of changes. */
	private final ChangeSequenceController<T> controller;

	/**
	 * Constructs an animation with a sequence of changes.
	 * @param original the original value
	 * @param sequence the sequence of changes
	 */
	public Animation(T original, ChangesSequence<T> sequence) {
		this.original = original;
		this.controller = ChangeSequenceController.newController(sequence);
	}
	/**
	 * Constructs an animation with a variable number of changes.
	 * @param original the original value
	 * @param sequence the sequence of changes
	 */
	@SafeVarargs
	public Animation(T original, T... sequence) {
		this.original = original;
		this.controller = ChangeSequenceController.newController(ChangesSequence.of(sequence));
	}

	/**
	 * Fetches the next change in the animation sequence.
	 * @return the next change
	 */
	public T fetchNextChange() {
		return controller.next();
	}

	/**
	 * Fetches the previous change in the animation sequence.
	 * @return the previous change
	 */
	public T fetchPreviousChange() {
		return controller.previous();
	}

	/**
	 * Gets the current index in the change sequence.
	 * @return current change index
	 */
	public int current() {
		return controller.getChangeIndex();
	}

}
