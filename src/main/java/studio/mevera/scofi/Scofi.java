package studio.mevera.scofi;

import studio.mevera.scofi.base.*;
import studio.mevera.scofi.base.impl.LegacyBoard;
import studio.mevera.scofi.base.impl.AdventureBoard;
import studio.mevera.scofi.util.FastReflection;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main manager class for Scofi, responsible for handling player boards and their updates.
 * <p>
 * This class manages the lifecycle of boards for online players, including creation, registration, updating,
 * and removal. It supports both legacy and modern board adapters, automatically detecting Adventure API support.
 * Boards are updated asynchronously at a configurable interval.
 * </p>
 * <p>
 * Usage example:
 * <pre>
 *     Scofi scofi = Scofi.load(plugin);
 *     scofi.setupNewBoard(player, adapter);
 *     scofi.startBoardUpdaters();
 * </pre>
 *
 * @since 1.0
 * @author Mqzen (aka Mqzn)
 */
public class Scofi {

	/** The plugin instance using Scofi. */
	private final @NotNull Plugin plugin;
	/** The task ID for board updates, if scheduled. */
	private @Nullable Integer updateTaskId = null;
	/** Map of player UUIDs to their boards. */
	private final @NotNull Map<UUID, BoardBase<?>> boards = new HashMap<>();
	/** Whether Adventure API support is available. */
	public static final boolean ADVENTURE_SUPPORT;
	/** Logger for Scofi operations. */
	private final @Getter Logger logger = Logger.getLogger(this.getClass().getSimpleName());

	static {
		ADVENTURE_SUPPORT = FastReflection
				.optionalClass("io.papermc.paper.adventure.PaperAdventure")
				.isPresent();
	}

	/** Board update interval in ticks. */
	private @Getter long updateInterval = 3L; // in ticks

	/**
	 * Private constructor for singleton pattern.
	 * @param plugin the plugin instance
	 */
	private Scofi(@NotNull Plugin plugin) {
		this.plugin = plugin;
	}

	/**
	 * Loads the Scofi instance into memory (singleton).
	 * @param plugin the plugin that's using Scofi
	 * @return the loaded Scofi instance
	 * @throws IllegalArgumentException if plugin is null
	 */
	public static Scofi load(Plugin plugin) {
		if(plugin == null )
			throw new IllegalArgumentException("Plugin cannot be null");
		return new Scofi(plugin);
	}

	/**
	 * Sets the update interval of the boards.
	 * If the updater is running, it will be restarted with the new interval.
	 * @param interval the interval in ticks
	 */
	public void setUpdateInterval(long interval) {
		if(updateTaskId == null) {
			this.updateInterval = interval;
			return;
		}
		Bukkit.getScheduler().cancelTask(updateTaskId);
		this.updateInterval = interval;
		this.startBoardUpdaters();
	}

	/**
	 * Fetches the board created for the player whose uuid matches that of the parameter.
	 * @param uuid the uuid of the player who is the owner of a board
	 * @return the board made for that player, or null if not registered
	 * @throws ClassCastException if the board type does not match
	 */
	@SuppressWarnings("unchecked")
	public @Nullable <T> BoardBase<T> getBoard(@NotNull UUID uuid) throws ClassCastException {
		return (BoardBase<T>) boards.get(uuid);
	}

	/**
	 * Registers a board for a player's uuid.
	 * @param uuid the uuid of the player
	 * @param mBoard the board to register
	 */
	private void registerBoard(UUID uuid, BoardBase<?> mBoard) {
		boards.put(uuid, mBoard);
	}

	/**
	 * Creates a new board and registers it for the player using an adapter.
	 * @param player the player to register
	 * @param adapter the board adapter
	 * @throws UnsupportedOperationException if modern adapter is used without Adventure support
	 * @throws IllegalStateException if legacy adapter is used in a modern MC version
	 */
	public void setupNewBoard(Player player, BoardAdapter<?> adapter) {
		if(adapter instanceof ModernBoardAdapter && !ADVENTURE_SUPPORT) {
			throw new UnsupportedOperationException("Use of modern board adapter is not supported in this mc version.");
		}
		BoardBase<?> board;
        if (ADVENTURE_SUPPORT) {
			if (!(adapter instanceof ModernBoardAdapter)) {
				throw new IllegalStateException("You cannot use legacy board adapter in a modern mc version !");
			}
			ModernBoardAdapter modernBoardAdapter = (ModernBoardAdapter)adapter;
            board = new AdventureBoard(this, player, modernBoardAdapter);
        } else {
            board = new LegacyBoard(this, player, (LegacyBoardAdapter) adapter);
        }
        registerBoard(player.getUniqueId(), board);
	}

	/**
	 * Deletes and unregisters the board for the given player.
	 * @param player the owner of the board
	 */
	public void removeBoard(@NotNull Player player) {
		BoardBase<?> board = getBoard(player.getUniqueId());
		if(board != null) {
			board.delete();
		}
		boards.remove(player.getUniqueId());
	}

	/**
	 * Starts the scheduled task for board updates.
	 * Boards are updated asynchronously every {@link #updateInterval} ticks.
	 * @see Scofi#setUpdateInterval(long)
	 */
	public void startBoardUpdaters() {
		updateTaskId = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, ()-> {
			for(BoardBase<?> board : boards.values()) {
				if(board.isDeleted())continue;
				BoardUpdate update = board.getUpdate();
				if(update == null) continue;
				try {
					update.update(board);
				}catch (Exception ex) {
					plugin.getLogger().log(Level.SEVERE, ex, ()-> "Failed to update " + board.getPlayer().getName() + "'s scoreboard.");
				}
			}
		}, 1L, updateInterval).getTaskId();
	}

	/**
	 * Stops the scheduled task for board updates.
	 * @see Scofi#startBoardUpdaters()
	 */
	public void stopBoardUpdaters() {
		if(updateTaskId != null)
			Bukkit.getScheduler().cancelTask(updateTaskId);
	}

}
