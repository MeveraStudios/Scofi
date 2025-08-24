package studio.mevera.scofi.animation;

import studio.mevera.scofi.animation.core.Animation;
import studio.mevera.scofi.animation.core.ChangesSequence;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

/**
 * Animation for scrolling text horizontally, creating a marquee effect.
 * <p>
 * Uses {@link Scroller} to animate the scrolling, supporting legacy color codes. Typically used for scoreboard or
 * text display animations where content needs to move or loop. Instances are created via static factory methods.
 * </p>
 * <p>
 * Usage example:
 * <pre>
 *     Animation<String> anim = ScrollAnimation.of("Welcome!", 10, 2);
 *     String frame = anim.fetchNextChange();
 * </pre>
 */
public final class ScrollAnimation extends Animation<String> {
	/** The scroller utility for animation. */
	private final @NotNull Scroller scroller;

	/**
	 * Constructs a scroll animation for the given text.
	 * @param original the text to scroll
	 * @param width the width of the scroll window
	 * @param spaceBetween spaces between scroll loops
	 */
	private ScrollAnimation(String original, int width, int spaceBetween) {
		super(original, ChangesSequence.of());
		this.scroller = Scroller.of(ChatColor.translateAlternateColorCodes('&', original), width, spaceBetween);
	}

	/**
	 * Creates a scroll animation for the given text.
	 * @param msg the text to scroll
	 * @param width the width of the scroll window
	 * @param spaceBetween spaces between scroll loops
	 * @return new ScrollAnimation instance
	 */
	public static ScrollAnimation of(String msg, int width, int spaceBetween) {
		return new ScrollAnimation(msg, width, spaceBetween);
	}

	/**
	 * Gets the next frame of the scroll animation.
	 * @return scrolled string for the next position
	 */
	@Override
	public String fetchNextChange() {
		return scroller.next();
	}

	/**
	 * Gets the previous frame of the scroll animation (same as next for this implementation).
	 * @return scrolled string for the previous position
	 */
	@Override
	public String fetchPreviousChange() {
		return scroller.next();
	}

}
