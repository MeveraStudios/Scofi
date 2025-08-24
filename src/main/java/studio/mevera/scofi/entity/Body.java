package studio.mevera.scofi.entity;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.ChatColor;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Represents the body of a board, which is a collection of lines (either legacy String or Adventure Component).
 * <p>
 * Provides methods for adding lines, retrieving all lines, and updating lines by index. Factory methods allow easy creation
 * of legacy or adventure bodies from lists or arrays of content. This abstraction is used to manage the main content area
 * of a board, separate from title or other metadata.
 * </p>
 * <p>
 * Usage examples:
 * <pre>
 *     Body<String> legacyBody = Body.legacy("&aLine1", "&bLine2");
 *     Body<Component> advBody = Body.adventure(Component.text("Line1"), Component.text("Line2"));
 * </pre>
 */
public interface Body<T> {
    /**
     * Adds a line to the body using raw content (String or Component).
     * @param content the line content
     */
    void addLine(T content);
    /**
     * Adds a line to the body using a Line instance.
     * @param line the line instance
     */
    void addLine(Line<T> line);
    /**
     * Gets all lines in the body.
     * @return list of lines
     */
    List<Line<T>> getLines();
    /**
     * Sets a line at the specified index, updating its index property.
     * Does nothing if index is out of bounds.
     * @param index the line index
     * @param line the line to set
     */
    default void setLine(int index, Line<T> line) {
        if(index < 0 || index >= getLines().size()) return;
        // Ensure the line has the correct index
        line.setIndex(index);
        getLines().set(index, line);
    }
    /**
     * Creates a legacy body from an array of strings.
     * @param lines array of legacy string lines
     * @return legacy body instance
     */
    static BodyImplementation.LegacyBody legacy(String... lines) {
        return legacy(Arrays.asList(lines));
    }
    /**
     * Creates a legacy body from a list of strings.
     * @param lines list of legacy string lines
     * @return legacy body instance
     */
    static BodyImplementation.LegacyBody legacy(List<String> lines) {
        return new BodyImplementation.LegacyBody(lines);
    }
    /**
     * Creates an adventure body from an array of components.
     * @param components array of adventure components
     * @return adventure body instance
     */
    static BodyImplementation.AdventureBody adventure(Component... components) {
        return adventure(Arrays.asList(components));
    }
    /**
     * Creates an adventure body from a list of components.
     * @param components list of adventure components
     * @return adventure body instance
     */
    static BodyImplementation.AdventureBody adventure(List<Component> components) {
        return new BodyImplementation.AdventureBody(components);
    }
    /**
     * Abstract base implementation for board bodies, managing a list of lines.
     * <p>
     * Use LegacyBody for String content, AdventureBody for Component content.
     * </p>
     */
    @Getter
    abstract class BodyImplementation<T> implements Body<T>{
        private final List<Line<T>> lines;
        /**
         * Initializes the body with an empty list of lines.
         */
        public BodyImplementation() {
            lines = new CopyOnWriteArrayList<>();
        }
        /**
         * Implementation for legacy bodies (String lines).
         */
        public static class LegacyBody extends BodyImplementation<String>{
            /**
             * Constructs a legacy body from a list of strings.
             * @param lines list of legacy string lines
             */
            public LegacyBody(List<String> lines) {
                super();
                for (String line : lines) {
                    addLine(line);
                }
            }
            /**
             * Adds a legacy line using raw string content.
             * @param content legacy string content
             */
            @Override
            public void addLine(String content) {
                int correctIndex = getLines().size();
                getLines().add(Line.legacy(ChatColor.translateAlternateColorCodes('&', content), correctIndex));
            }
            /**
             * Adds a legacy line using a Line instance.
             * @param line legacy line instance
             */
            @Override
            public void addLine(Line<String> line) {
                // FIXED: Always ensure the line gets the correct index based on its position
                int correctIndex = getLines().size();
                line.setIndex(correctIndex);
                getLines().add(line);
            }
        }
        /**
         * Implementation for adventure bodies (Component lines).
         */
        public static class AdventureBody extends BodyImplementation<Component>{
            /**
             * Constructs an adventure body from a list of components.
             * @param lines list of adventure components
             */
            public AdventureBody(List<Component> lines) {
                super();
                for (Component line : lines) {
                    addLine(line);
                }
            }
            /**
             * Adds an adventure line using raw component content.
             * @param content adventure component
             */
            @Override
            public void addLine(Component content) {
                int correctIndex = getLines().size();
                getLines().add(Line.adventure(content, correctIndex));
            }
            /**
             * Adds an adventure line using a Line instance.
             * @param line adventure line instance
             */
            @Override
            public void addLine(Line<Component> line) {
                // FIXED: Always ensure the line gets the correct index based on its position
                int correctIndex = getLines().size();
                line.setIndex(correctIndex);
                getLines().add(line);
            }
        }
    }
}