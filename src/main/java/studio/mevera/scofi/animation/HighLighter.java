package studio.mevera.scofi.animation;

import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for generating highlighted text animations.
 * <p>
 * Highlights a single character in a string with a secondary color, while the rest of the text uses a primary color.
 * Used for creating animated effects in scoreboards and other text displays. Supports both legacy color codes and
 * Bukkit ChatColor objects. Instances are created via static factory methods.
 * </p>
 * <p>
 * Usage example:
 * <pre>
 *     HighLighter highlighter = HighLighter.of("Hello", "&a", "&b");
 *     String animated = highlighter.nextResult();
 * </pre>
 */
public final class HighLighter {

	/** The text to highlight. */
	private final StringBuilder text;
	/** The primary color code for non-highlighted text. */
	private final String primaryColor, secondaryColor;
	/** Stores all highlighted results (not used in animation, but available). */
	private final List<String> highLighted = new ArrayList<>();
	/** Current highlight position. */
	private int position;
	/** Length of the text. */
	private final int limit;

	/**
	 * Constructs a HighLighter with legacy color codes.
	 * @param text the text to highlight
	 * @param primaryColor primary color code (e.g. "&a")
	 * @param secondaryColor secondary color code (e.g. "&b")
	 */
	private HighLighter(String text, String primaryColor, String secondaryColor) {

		this.text = new StringBuilder(text);
		this.primaryColor = ChatColor.translateAlternateColorCodes('&', primaryColor);
		this.secondaryColor = ChatColor.translateAlternateColorCodes('&', secondaryColor);
		this.limit = text.length();
	}

	/**
	 * Constructs a HighLighter with Bukkit ChatColor objects.
	 * @param text the text to highlight
	 * @param primary primary color
	 * @param secondary secondary color
	 */
	private HighLighter(String text, ChatColor primary, ChatColor secondary) {
		this(text, primary.toString(), secondary.toString());
	}

	/**
	 * Generates the next highlighted result, cycling through the text.
	 * @return the highlighted string for the current position
	 */
	public String nextResult() {
		StringBuilder builder = new StringBuilder();

		if (position >= limit) {
			position = 0;
		}

		if (position > 0) {
			builder.append(primaryColor).append(text, 0, position);
		}
		String secondaryTarget = text.substring(position, position + 1);
		builder.append(secondaryColor).append(secondaryTarget);

		if (position < limit-1) {
			builder.append(primaryColor).append(text.substring(position+1));
		}

		position++;
		return builder.toString();
	}

	/**
	 * Gets the list of all highlighted results (not animated).
	 * @return list of highlighted strings
	 */
	public @NotNull List<String> getHighLighted() {
		return highLighted;
	}

	/**
	 * Creates a HighLighter using legacy color codes.
	 * @param text the text to highlight
	 * @param primaryColor primary color code
	 * @param secondaryColor secondary color code
	 * @return new HighLighter instance
	 */
	public static HighLighter of(String text, String primaryColor, String secondaryColor) {
		return new HighLighter(text, primaryColor, secondaryColor);
	}

	/**
	 * Creates a HighLighter using Bukkit ChatColor objects.
	 * @param text the text to highlight
	 * @param primary primary color
	 * @param secondary secondary color
	 * @return new HighLighter instance
	 */
	public static HighLighter of(String text, ChatColor primary, ChatColor secondary) {
		return new HighLighter(text, primary, secondary);
	}

	/**
	 * Gets a highlighted result by index from the stored list.
	 * @param index the index of the highlighted result
	 * @return highlighted string at the given index
	 */
	public String getHighLighted(int index) {
		return this.highLighted.get(index);
	}

}