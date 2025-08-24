package studio.mevera.scofi.base;

/**
 * Functional interface for board update actions.
 * <p>
 * Implement this interface to define custom update logic for a board, typically used in scheduled tasks.
 * The {@code update} method is called to perform updates such as refreshing the title or body.
 * </p>
 * <p>
 * Usage example:
 * <pre>
 *     BoardUpdate update = board -> {
 *         board.updateTitle();
 *         board.updateLines();
 *     };
 * </pre>
 */
@FunctionalInterface
public interface BoardUpdate  {
	/**
	 * Executes update actions for the given board.
	 * @param board the board to be updated
	 */
	void update(BoardBase<?> board);

}
