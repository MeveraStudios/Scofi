package studio.mevera.scofi.entity;

import studio.mevera.scofi.animation.HighlightingAnimation;
import studio.mevera.scofi.animation.ScrollAnimation;
import studio.mevera.scofi.animation.core.Animation;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;

/**
 * Represents a single line in a board or display, supporting both legacy (String) and modern (Adventure Component) formats.
 * <p>
 * Provides builder factories for creating lines with optional animations (highlighting, scrolling, or custom).
 * This interface is central to the board rendering system, abstracting content, index, and animation logic.
 * </p>
 * <p>
 * Usage examples:
 * <pre>
 *     Line<String> line = Line.legacy("&aHello World").withHighlight("&a", "&b").build();
 *     Line<Component> advLine = Line.adventure(Component.text("Hello")).withScroll(10, 2).build();
 * </pre>
 */
public interface Line<T> {
    /**
     * Gets the content of the line (String or Component).
     */
    T getContent();
    /**
     * Sets the content of the line.
     */
    void setContent(T content);
    /**
     * Gets the index of the line in the board.
     */
    int getIndex();
    /**
     * Sets the index of the line in the board.
     */
    void setIndex(int index);
    /**
     * Gets the animation associated with this line, if any.
     */
    Animation<T> getAnimation();
    /**
     * Sets the animation for this line.
     */
    void setAnimation(Animation<T> animation);
    /**
     * Fetches the current content, applying animation if present.
     * @return the animated or static content
     */
    default T fetchContent(){
        return getAnimation() == null ? getContent() : getAnimation().fetchNextChange();
    }
    /**
     * Creates a builder for a legacy (String) line.
     * @param content the legacy string content
     * @return builder for legacy line
     */
    static LegacyLineBuilder legacy(String content){
        return new LegacyLineBuilder(content);
    }
    /**
     * Creates a builder for an adventure (Component) line.
     * @param content the adventure component content
     * @return builder for adventure line
     */
    static AdventureLineBuilder adventure(Component content){
        return new AdventureLineBuilder(content);
    }
    /**
     * Creates a legacy line directly (backwards compatibility).
     * @param content legacy string content
     * @param index line index
     * @return legacy line instance
     */
    static LineImplementation.LegacyLine legacy(String content, int index){
        return new LineImplementation.LegacyLine(content, index);
    }
    /**
     * Creates an adventure line directly (backwards compatibility).
     * @param content adventure component content
     * @param index line index
     * @return adventure line instance
     */
    static LineImplementation.AdventureLine adventure(Component content, int index){
        return new LineImplementation.AdventureLine(content, index);
    }
    /**
     * Builder for legacy (String) lines, supporting optional animations.
     */
    class LegacyLineBuilder {
        private final String content;
        private Animation<String> animation;
        /**
         * Initializes the builder with legacy content, translating color codes.
         * @param content legacy string content
         */
        public LegacyLineBuilder(String content) {
            this.content = ChatColor.translateAlternateColorCodes('&', content);
        }
        /**
         * Adds a highlighting animation using color codes.
         * @param primaryColor primary color code (e.g. "&a")
         * @param secondaryColor secondary color code (e.g. "&b")
         * @return this builder
         */
        public LegacyLineBuilder withHighlight(String primaryColor, String secondaryColor) {
            this.animation = HighlightingAnimation.of(this.content, primaryColor, secondaryColor);
            return this;
        }
        /**
         * Adds a highlighting animation using ChatColor objects.
         * @param primary primary color
         * @param secondary secondary color
         * @return this builder
         */
        public LegacyLineBuilder withHighlight(ChatColor primary, ChatColor secondary) {
            this.animation = HighlightingAnimation.of(this.content, primary, secondary);
            return this;
        }
        /**
         * Adds a scroll animation to the line's content.
         * @param width scroll width
         * @param spaceBetween space between scrolls
         * @return this builder
         */
        public LegacyLineBuilder withScroll(int width, int spaceBetween) {
            this.animation = ScrollAnimation.of(this.content, width, spaceBetween);
            return this;
        }
        /**
         * Adds a custom animation (must use the line's content as base).
         * @param customAnimation custom animation instance
         * @return this builder
         * @throws IllegalStateException if animation's base content does not match
         */
        public LegacyLineBuilder withAnimation(Animation<String> customAnimation) {
            // Validate that the animation is based on this line's content
            if (customAnimation != null && !this.content.equals(customAnimation.getOriginal())) {
                throw new IllegalStateException("Animation's content '" + customAnimation.getOriginal() + "' does not match the line's content '" + content + "'");
            }
            this.animation = customAnimation;
            return this;
        }
        
        /**
         * Adds a custom animation to the line using the provided original content and sequence.
         * Each string in the sequence will have color codes translated.
         *
         * @param originalContent the base content for the animation, with color codes
         * @param sequence the sequence of strings for the animation, each with color codes
         * @return this builder with the animation applied
         */
        public LegacyLineBuilder withAnimation(String originalContent, String... sequence) {
            String[] coloredSequence = java.util.Arrays.stream(sequence)
                    .map(s -> ChatColor.translateAlternateColorCodes('&', s))
                    .toArray(String[]::new);
            return withAnimation(new Animation<>(ChatColor.translateAlternateColorCodes('&', originalContent), coloredSequence));
        }
        
        /**
         * Builds the legacy line instance.
         * @return legacy line
         */
        public LineImplementation.LegacyLine build() {
            LineImplementation.LegacyLine line = new LineImplementation.LegacyLine(content, -1);
            if (animation != null) {
                line.setAnimation(animation);
            }
            return line;
        }
    }
    /**
     * Builder for adventure (Component) lines, supporting optional animations.
     */
    class AdventureLineBuilder {
        private final Component content;
        private Animation<Component> animation;
        /**
         * Initializes the builder with adventure component content.
         * @param content adventure component
         */
        public AdventureLineBuilder(Component content) {
            this.content = content;
        }
        /**
         * Adds a highlighting animation to the line's content.
         * Note: For components, this converts to legacy format for animation.
         * @param primary primary color
         * @param secondary secondary color
         * @return this builder
         */
        public AdventureLineBuilder withHighlight(ChatColor primary, ChatColor secondary) {
            // Convert component to legacy for highlighting animation
            // This is a limitation - proper component animation would need a component-aware highlighter
            String legacy = net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
                    .legacySection().serialize(this.content);
            // Create animation and convert back
            Animation<String> legacyAnim = HighlightingAnimation.of(legacy, primary, secondary);
            this.animation = new Animation<Component>(this.content) {
                @Override
                public Component fetchNextChange() {
                    String animatedLegacy = legacyAnim.fetchNextChange();
                    return net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
                            .legacySection().deserialize(animatedLegacy);
                }
            };
            return this;
        }
        /**
         * Adds a scroll animation to the line's content.
         * @param width scroll width
         * @param spaceBetween space between scrolls
         * @return this builder
         */
        public AdventureLineBuilder withScroll(int width, int spaceBetween) {
            String legacy = net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
                    .legacySection().serialize(this.content);
            Animation<String> legacyAnim = ScrollAnimation.of(legacy, width, spaceBetween);
            this.animation = new Animation<Component>(this.content) {
                @Override
                public Component fetchNextChange() {
                    String animatedLegacy = legacyAnim.fetchNextChange();
                    return net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
                            .legacySection().deserialize(animatedLegacy);
                }
            };
            return this;
        }
        /**
         * Adds a custom animation to the line's content.
         * @param customAnimation custom animation instance
         * @return this builder
         */
        public AdventureLineBuilder withAnimation(Animation<Component> customAnimation) {
            this.animation = customAnimation;
            return this;
        }
        /**
         * Builds the adventure line instance.
         * @return adventure line
         */
        public LineImplementation.AdventureLine build() {
            LineImplementation.AdventureLine line = new LineImplementation.AdventureLine(content, -1);
            if (animation != null) {
                line.setAnimation(animation);
            }
            return line;
        }
    }
    /**
     * Implementation of Line for both legacy and adventure formats.
     * <p>
     * Use LegacyLine for String content, AdventureLine for Component content.
     * </p>
     */
    @Getter
    @Setter
    class LineImplementation<T> implements Line<T>{
        private T content;
        private int index;
        private Animation<T> animation;
        /**
         * Constructs a line implementation.
         * @param content line content
         * @param index line index
         */
        public LineImplementation(T content, int index) {
            this.content = content;
            this.index = index;
            this.animation = null;
        }
        /**
         * Legacy line implementation (String content).
         */
        public static class LegacyLine extends LineImplementation<String>{
            public LegacyLine(String content, int index) {
                super(content, index);
            }
        }
        /**
         * Adventure line implementation (Component content).
         */
        public static class AdventureLine extends LineImplementation<Component>{
            public AdventureLine(Component content, int index) {
                super(content, index);
            }
        }
    }
}