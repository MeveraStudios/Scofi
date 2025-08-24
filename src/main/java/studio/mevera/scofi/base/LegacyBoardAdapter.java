package studio.mevera.scofi.base;

/**
 * Adapter interface for legacy boards using String-based lines.
 * <p>
 * Extends {@link BoardAdapter} with a String type parameter, providing compatibility for legacy scoreboard systems
 * that use color-coded string lines. Implement this interface to support legacy board rendering and updates.
 * </p>
 * <p>
 * Usage example:
 * <pre>
 *     public class MyLegacyBoardAdapter implements LegacyBoardAdapter {
 *         // Implement required BoardAdapter methods for String lines
 *     }
 * </pre>
 */
public interface LegacyBoardAdapter extends BoardAdapter<String> {
}
