package studio.mevera.scofi.entity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import studio.mevera.scofi.animation.core.Animation;

import java.util.Optional;

/**
 * Implementation of Title for both legacy and adventure formats.
 */
abstract class AbstractTitle<T> implements Title<T> {
	
	T content;
	private Animation<T> titleAnimation;
	
	/**
	 * Default constructor.
	 */
	AbstractTitle() {}
	/**
	 * Constructs a title with content.
	 * @param content the title content
	 */
	AbstractTitle(T content) {
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
			if(this instanceof LegacyTitle) {
				this.content = (T) org.bukkit.ChatColor.translateAlternateColorCodes('&', (String) titleAnimation.getOriginal());
				return;
			}
			this.content = titleAnimation.getOriginal();
		}
	}
	/**
	 * Sets an animation for the title and returns the instance.
	 * @param animation the animation
	 * @return the title instance with animation
	 */
	@Override
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
	
}