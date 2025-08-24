package studio.mevera.scofi.animation;

import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for generating horizontal scrolling (marquee) text frames.
 * <p>
 * Used to create animated scrolling effects for scoreboards and text displays. Handles legacy color codes and
 * configurable window width and spacing between scroll loops. Instances are created via static factory methods.
 * </p>
 * <p>
 * Usage example:
 * <pre>
 *     Scroller scroller = Scroller.of("Welcome!", 10, 2);
 *     String frame = scroller.next();
 * </pre>
 */
public final class Scroller {

	/** Current scroll position. */
	private int position;
	/** List of all scroll frames. */
	private final List<String> list;
	/** Current color for the scroll frame. */
	private ChatColor color = ChatColor.RESET;

	/**
	 * Constructs a Scroller for the given message, width, and spacing.
	 * @param message the text to scroll
	 * @param width the width of the scroll window
	 * @param spaceBetween spaces between scroll loops
	 */
	private Scroller(String message, int width, int spaceBetween) {
		message = ChatColor.translateAlternateColorCodes('&', message);
		list = new ArrayList<>();
		// String is too short for window?
		if (message.length() < width) {
			StringBuilder sb = new StringBuilder(message);
			while (sb.length() < width)
				sb.append(" ");
			message = sb.toString();
		}
		// Allow for colours which add 2 to the width
		width -= 2;
		// Invalid width/space size
		if (width < 1)
			width = 1;

		if (spaceBetween < 0)
			spaceBetween = 0;
		// Add substrings
		for (int i = 0; i < message.length() - width; i++)
			list.add(message.substring(i, i + width));
		// Add space between repeats
		StringBuilder space = new StringBuilder();
		for (int i = 0; i < spaceBetween; ++i) {
			list.add(message.substring(message.length() - width + (Math.min(i, width))) + space);
			if (space.length() < width)
				space.append(" ");
		}
		// Wrap
		for (int i = 0; i < width - spaceBetween; ++i)
			list.add(message.substring(message.length() - width + spaceBetween + i) + space + message.substring(0, i));
		// Join up
		for (int i = 0; i < spaceBetween; i++) {
			if (i > space.length())
				break;
			list.add(space.substring(0, space.length() - i) + message.substring(0, width - (Math.min(spaceBetween, width)) + i));
		}
	}

	/**
	 * Creates a Scroller for the given message, width, and spacing.
	 * @param message the text to scroll
	 * @param width the width of the scroll window
	 * @param spaceBetween spaces between scroll loops
	 * @return new Scroller instance
	 */
	public static @NotNull Scroller of(String message, int width, int spaceBetween) {
		return new Scroller(message, width, spaceBetween);
	}

	/**
	 * Gets the next frame of the scroll animation.
	 * @return scrolled string for the next position
	 */
	public String next() {
		StringBuilder sb = getNext();
		if (sb.charAt(sb.length() - 1) == ChatColor.COLOR_CHAR) {
			sb.setCharAt(sb.length() - 1, ' ');
		}
		if (sb.charAt(0) == ChatColor.COLOR_CHAR) {
			ChatColor c = ChatColor.getByChar(sb.charAt(1));
			if (c != null) {
				color = c;
				sb = getNext();
				if (sb.charAt(0) != ' ') {
					sb.setCharAt(0, ' ');
				}
			}
		}
		return color + sb.toString();
	}

	/**
	 * Gets the next scroll frame as a StringBuilder.
	 * @return next scroll frame
	 */
	private StringBuilder getNext() {
		return new StringBuilder(list.get(position++ % list.size()));
	}


}