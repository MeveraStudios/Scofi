package studio.mevera.scofi.base;

import net.kyori.adventure.text.Component;

/**
 * Adapter interface for modern boards using Adventure {@link Component}-based lines.
 * <p>
 * Extends {@link BoardAdapter} with a Component type parameter, providing compatibility for modern scoreboard systems
 * that use Adventure API components for rich text. Implement this interface to support modern board rendering and updates.
 * </p>
 * <p>
 * Usage example:
 * <pre>
 *     public class MyModernBoardAdapter implements ModernBoardAdapter {
 *         // Implement required BoardAdapter methods for Component lines
 *     }
 * </pre>
 */
public interface ModernBoardAdapter extends BoardAdapter<Component> {

}
