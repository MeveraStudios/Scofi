package studio.mevera.scofi.entity;

import org.bukkit.ChatColor;
import studio.mevera.scofi.animation.HighlightingAnimation;
import studio.mevera.scofi.animation.ScrollAnimation;
import studio.mevera.scofi.animation.core.Animation;

import java.util.Arrays;

/**
 * Builder for legacy titles (String content).
 */
public final class LegacyTitle extends AbstractTitle<String> {
	/**
	 * Sets the legacy text for the title, translating color codes.
	 * @param content legacy string content
	 * @return this builder
	 */
	public LegacyTitle ofText(String content) {
		super.setContent(ChatColor.translateAlternateColorCodes('&', content));
		return this;
	}
	/**
	 * Adds a scroll animation to the legacy title.
	 * @param width scroll width
	 * @param spaceBetween space between scrolls
	 * @return this builder
	 */
	public LegacyTitle withScroll(int width, int spaceBetween) {
		if(this.content == null) {
			throw new IllegalArgumentException("You cannot call withScroll() without calling #ofText() before it to set the text, " +
				"the scrolling animation will be based on no text," +
				" Alternatively you can set it using #withAnimation");
		}
		return (LegacyTitle) super.withAnimation(ScrollAnimation.of(this.content, width, spaceBetween));
	}
	/**
	 * Adds a highlighting animation to the legacy title.
	 * @param primaryColor primary color
	 * @param secondaryColor secondary color
	 * @return this builder
	 */
	public LegacyTitle withHighlight(org.bukkit.ChatColor primaryColor, org.bukkit.ChatColor secondaryColor) {
		if(this.content == null) {
			throw new IllegalArgumentException("You cannot call withHighlight() without calling #ofText() before it to set the text, " +
				"the scrolling animation will be based on no text," +
				" Alternatively you can set it using #withAnimation");
		}
		return (LegacyTitle) super.withAnimation(HighlightingAnimation.of(this.content, primaryColor, secondaryColor));
	}
	
	/**
	 * Adds a custom animation to the legacy title using the provided original content and sequence.
	 * Each string in the sequence will have color codes translated.
	 *
	 * @param originalContent the base content for the animation, with color codes
	 * @param sequence the sequence of strings for the animation, each with color codes
	 * @return this builder with the animation applied
	 */
	public LegacyTitle withAnimation(String originalContent, String... sequence) {
		String[] coloredSequence = Arrays.stream(sequence)
				.map(s -> ChatColor.translateAlternateColorCodes('&', s))
				.toArray(String[]::new);
		return (LegacyTitle) super.withAnimation(
				new Animation<>(ChatColor.translateAlternateColorCodes('&', originalContent), coloredSequence)
		);
	}
}