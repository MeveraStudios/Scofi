package studio.mevera.scofi.entity;

import studio.mevera.scofi.animation.HighlightingAnimation;
import studio.mevera.scofi.animation.ScrollAnimation;
import studio.mevera.scofi.animation.core.Animation;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.ChatColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Optional;

/**
 * Represents the title of a scoreboard, supporting both legacy (String) and modern (Adventure Component) formats.
 * <p>
 * Provides methods for retrieving the title content, setting animations, and building title instances for legacy and adventure boards.
 * Used by {@link studio.mevera.scofi.base.BoardAdapter} to supply the title for a board.
 * </p>
 * <p>
 * Usage example:
 * <pre>
 *     Title<String> legacyTitle = Title.legacy().ofText("&aScoreboard Title").withHighlight(ChatColor.GREEN, ChatColor.YELLOW);
 *     Title<Component> advTitle = Title.adventure().ofComponent(Component.text("Scoreboard Title"));
 * </pre>
 *
 * @param <T> the type of content (String or Component)
 * @since 1.0
 * @author Mqzen (aka Mqzn)
 */
public interface Title<T> {
	/**
	 * Gets the content of the title, applying animation if present.
	 * @return optional title content
	 */
	Optional<T> get();
	/**
	 * Sets an animation for the title.
	 * @param animation the animation
	 * @return the title instance with animation
	 */
	Title<T> withAnimation(@Nullable Animation<T> animation);
	
	/**
	 * Loads the animation for the title, if present.
	 * @return optional animation
	 */
	default Optional<Animation<T>> loadAnimation() {
		return Optional.empty();
	}
	/**
	 * Creates a legacy title builder.
	 * @return legacy title instance
	 */
	static LegacyTitle legacy() {
		return new LegacyTitle();
	}
	/**
	 * Creates an adventure title builder.
	 * @return adventure title instance
	 */
	static AdventureTitle adventure() {
		return new AdventureTitle();
	}
	/**
	 * Checks if the title has an animation.
	 * @return true if animated
	 */
	default boolean hasAnimation() {
		return loadAnimation().isPresent();
	}
	
	/**
	 * Sets the animation for the title.
	 * @param titleAnimation the animation
	 */
	void setTitleAnimation(Animation<T> titleAnimation);
	
}