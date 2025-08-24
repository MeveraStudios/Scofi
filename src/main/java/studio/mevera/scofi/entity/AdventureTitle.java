package studio.mevera.scofi.entity;

import net.kyori.adventure.text.Component;

/**
 * Builder for adventure titles (Component content).
 */
public final class AdventureTitle extends AbstractTitle<Component> {
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