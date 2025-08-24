package studio.mevera.scofi.base;

import studio.mevera.scofi.entity.Body;
import studio.mevera.scofi.entity.Title;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Adapter interface for providing board data and update logic.
 * <p>
 * Implement this interface to supply the title, body, and update actions for a scoreboard or board display.
 * Supports both legacy (String) and modern (Component) types via generics. Used by Scofi to render and update boards for players.
 * </p>
 * <p>
 * Usage example:
 * <pre>
 *     public class MyBoardAdapter implements BoardAdapter<String> {
 *         // Implement getTitle, getBody, getBoardUpdate
 *     }
 * </pre>
 *
 * @param <T> the type of content (String or Component)
 */
public interface BoardAdapter<T> {

	/**
	 * Fetches the title to be represented on the board for the given player.
	 * @param player the player who will view the title
	 * @return the title of the scoreboard
	 */
	@NotNull
	Title<T> getTitle(Player player);

	/**
	 * Gets the body to be represented as the body of the scoreboard for the given player.
	 * @param player the player who will view the lines
	 * @return the body of the scoreboard
	 */
	@NotNull
	Body<T> getBody(Player player);

	/**
	 * Returns an update action if the board has any type of animations.
	 * Recommended implementation:
	 * <pre>
	 *   return (board) -> {
	 *     board.updateTitle();
	 *     board.updateBody();
	 *   };
	 * </pre>
	 * @return the actions to be executed as an update to the board
	 */
	@Nullable
	default BoardUpdate getBoardUpdate() {
		return BoardBase::update;
	}

}
