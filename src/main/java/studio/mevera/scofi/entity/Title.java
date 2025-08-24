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
	static TitleImplementation.LegacyTitle legacy() {
		return new TitleImplementation.LegacyTitle();
	}
	/**
	 * Creates an adventure title builder.
	 * @return adventure title instance
	 */
	static TitleImplementation.AdventureTitle adventure() {
		return new TitleImplementation.AdventureTitle();
	}
	/**
	 * Checks if the title has an animation.
	 * @return true if animated
	 */
	default boolean hasAnimation() {
		return loadAnimation().isPresent();
	}
	/**
	 * Implementation of Title for both legacy and adventure formats.
	 */
	class TitleImplementation<T> implements Title<T>{
		protected T content;
		private Animation<T> titleAnimation;
		/**
		 * Default constructor.
		 */
		public TitleImplementation() {}
		/**
		 * Constructs a title with content.
		 * @param content the title content
		 */
		public TitleImplementation(T content) {
			this.content = content;
		}
		/**
		 * Gets the content, applying animation if present.
		 * @return optional title content
		 */
		@Override
		public @NotNull Optional<T> get() {
			// FIXED: Check for animation first, similar to Line.fetchContent()
			if (titleAnimation != null) {
				return Optional.of(titleAnimation.fetchNextChange());
			}
			return Optional.of(content);
		}
		/**
		 * Sets the content of the title.
		 * @param content the title content
		 */
		public void setContent(T content) {
			if(titleAnimation != null) {
				throw new IllegalStateException("You cannot set a title content while being based on animation");
			}
			this.content = content;
		}
		/**
		 * Sets the animation for the title.
		 * @param titleAnimation the animation
		 */
		public void setTitleAnimation(Animation<T> titleAnimation) {
			this.titleAnimation = titleAnimation;
			if(titleAnimation != null) {
				this.content = titleAnimation.getOriginal();
			}
		}
		/**
		 * Sets an animation for the title and returns the instance.
		 * @param animation the animation
		 * @return the title instance with animation
		 */
		@Override @SuppressWarnings("unchecked")
		public Title<T> withAnimation(@Nullable Animation<T> animation) {
			setTitleAnimation(animation);
			return this;
		}
		/**
		 * Loads the animation for the title, if present.
		 * @return optional animation
		 */
		@Override
		public Optional<Animation<T>> loadAnimation() {
			return Optional.ofNullable(titleAnimation);
		}
		/**
		 * Builder for legacy titles (String content).
		 */
		public static class LegacyTitle extends TitleImplementation<String> {
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
		}
		/**
		 * Builder for adventure titles (Component content).
		 */
		public static class AdventureTitle extends TitleImplementation<Component> {
			/**
			 * Sets the adventure component for the title.
			 * @param content adventure component
			 * @return this builder
			 */
			public AdventureTitle ofComponent(Component content) {
				super.setContent(content);
				return this;
			}
		}
	}
}