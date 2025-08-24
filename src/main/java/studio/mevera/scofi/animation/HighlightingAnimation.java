package studio.mevera.scofi.animation;

import studio.mevera.scofi.animation.core.Animation;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

/**
 * Animation for highlighting a single character in a string, cycling through the text.
 * <p>
 * Uses {@link HighLighter} to animate the highlighting effect, supporting both legacy color codes and Bukkit ChatColor.
 * Typically used for scoreboard or text display animations. Instances are created via static factory methods.
 * </p>
 * <p>
 * Usage example:
 * <pre>
 *     Animation<String> anim = HighlightingAnimation.of("Hello", "&a", "&b");
 *     String frame = anim.fetchNextChange();
 * </pre>
 */
public final class HighlightingAnimation extends Animation<String> {

	/** The highlighter utility for animation. */
	private final @NotNull HighLighter highLighter;
	/** Current position for previous change. */
	private int position=0;

	/**
	 * Constructs a highlighting animation using legacy color codes.
	 * @param message the text to animate
	 * @param primaryColor primary color code
	 * @param secondaryColor secondary color code
	 */
	private HighlightingAnimation(@NotNull String message,
	                              @NotNull String primaryColor,
	                              @NotNull String secondaryColor) {
		super(message);
		this.highLighter = HighLighter.of(message, primaryColor, secondaryColor);
	}

	/**
	 * Constructs a highlighting animation using Bukkit ChatColor.
	 * @param message the text to animate
	 * @param primaryColor primary color
	 * @param secondaryColor secondary color
	 */
	private HighlightingAnimation(@NotNull String message,
	                              @NotNull ChatColor primaryColor,
	                              @NotNull ChatColor secondaryColor) {
		super(message);
		this.highLighter = HighLighter.of(message, primaryColor, secondaryColor);
	}

	/**
	 * Creates a highlighting animation using Bukkit ChatColor.
	 * @param message the text to animate
	 * @param primaryColor primary color
	 * @param secondaryColor secondary color
	 * @return new HighlightingAnimation instance
	 */
	public static HighlightingAnimation of(@NotNull String message,
	                                       @NotNull ChatColor primaryColor,
	                                       @NotNull ChatColor secondaryColor) {
		return new HighlightingAnimation(message, primaryColor, secondaryColor);
	}

	/**
	 * Creates a highlighting animation using legacy color codes.
	 * @param message the text to animate
	 * @param primaryColor primary color code
	 * @param secondaryColor secondary color code
	 * @return new HighlightingAnimation instance
	 */
	public static HighlightingAnimation of(@NotNull String message,
	                                       @NotNull String primaryColor,
	                                       @NotNull String secondaryColor) {
		return new HighlightingAnimation(message, primaryColor, secondaryColor);
	}

	/**
	 * Gets the next frame of the highlighting animation.
	 * @return highlighted string for the next position
	 */
	@Override
	public String fetchNextChange() {
		return this.highLighter.nextResult();
	}

	/**
	 * Gets the previous frame of the highlighting animation.
	 * @return highlighted string for the previous position
	 */
	@Override
	public String fetchPreviousChange() {
		if(position < 0) {
			position = 0;
		}
		String prev = highLighter.getHighLighted(position);
		position--;
		return prev;
	}

}
