package studio.mevera.scofi.animation;

import studio.mevera.scofi.animation.core.Animation;
import studio.mevera.scofi.animation.core.ChangesSequence;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

/**
 * Animation for scrolling text horizontally with stable width.
 * Maintains consistent width to prevent scoreboard resizing when used in titles.
 */
public final class ScrollAnimation extends Animation<String> {
	
	private final @NotNull Scroller scroller;
	private final int minWidth;
	private final boolean maintainWidth;
	
	/**
	 * Creates a scroll animation with optional width stabilization
	 * @param original the text to scroll
	 * @param width the width of the scroll window
	 * @param spaceBetween spaces between scroll loops
	 * @param maintainWidth whether to maintain consistent width
	 */
	private ScrollAnimation(String original, int width, int spaceBetween, boolean maintainWidth) {
		super(original, ChangesSequence.of());
		this.scroller = Scroller.of(ChatColor.translateAlternateColorCodes('&', original), width, spaceBetween);
		this.maintainWidth = maintainWidth;
		this.minWidth = maintainWidth ? Math.max(original.length(), width + 5) : width;
	}
	
	/**
	 * Creates a scroll animation (maintains width by default for titles)
	 * @param msg the text to scroll
	 * @param width the width of the scroll window
	 * @param spaceBetween spaces between scroll loops
	 * @return new ScrollAnimation instance
	 */
	public static ScrollAnimation of(String msg, int width, int spaceBetween) {
		return new ScrollAnimation(msg, width, spaceBetween, true);
	}
	
	/**
	 * Creates a scroll animation with explicit width maintenance control
	 * @param msg the text to scroll
	 * @param width the width of the scroll window
	 * @param spaceBetween spaces between scroll loops
	 * @param maintainWidth whether to maintain consistent width
	 * @return new ScrollAnimation instance
	 */
	public static ScrollAnimation of(String msg, int width, int spaceBetween, boolean maintainWidth) {
		return new ScrollAnimation(msg, width, spaceBetween, maintainWidth);
	}
	
	@Override
	public String fetchNextChange() {
		String scrolled = scroller.next();
		
		if (maintainWidth) {
			// Calculate actual visible length
			String stripped = ChatColor.stripColor(scrolled);
			int currentLength = stripped.length();
			
			if (currentLength < minWidth) {
				// Add invisible padding to maintain width
				StringBuilder result = new StringBuilder(scrolled);
				
				// Add spaces to reach minimum width
				for (int i = currentLength; i < minWidth; i++) {
					result.append(" ");
				}
				
				return result.toString();
			}
		}
		
		return scrolled;
	}
	
	@Override
	public String fetchPreviousChange() {
		String scrolled = scroller.next();
		
		if (maintainWidth) {
			// Apply same padding logic
			String stripped = ChatColor.stripColor(scrolled);
			int currentLength = stripped.length();
			
			if (currentLength < minWidth) {
				StringBuilder result = new StringBuilder(scrolled);
				for (int i = currentLength; i < minWidth; i++) {
					result.append(" ");
				}
				return result.toString();
			}
		}
		
		return scrolled;
	}
}