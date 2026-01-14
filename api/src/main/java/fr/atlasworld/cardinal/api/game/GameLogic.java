package fr.atlasworld.cardinal.api.game;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents the core logic of a game. This interface provides the necessary methods
 * for the lifecycle of a game, including initialization, periodic updates,
 * completion logic, and cleanup operations.
 * <p>
 * If the game-logic throws an exception when handling a game lifecycle event it will call {@link #interrupt(GameContext, Throwable)}.
 */
public interface GameLogic {

    /**
     * Called when the game is initializing.
     *
     * @param ctx game context.
     */
    void initialize(@NotNull GameContext ctx);

    /**
     * Called every tick once the game is {@link GameContainer.State#RUNNING}.
     * <p>
     * This is called on every tick,
     * for performance reasons you shouldn't do expensive updates on every tick.
     * <br>
     * Divide your updates between multiple ticks, like update the scoreboard only every 20 ticks.
     *
     * @param ctx game context.
     * @return {@code true} if the game should end, {@code false} if the game should continue running.
     */
    boolean update(@NotNull GameContext ctx);

    /**
     * Called once the game has finished,
     * players will be kicked out of the game after this function finishes.
     * <p>
     * Usually empty unless you want to execute special logic before players are kicked.
     *
     * @param ctx game context.
     */
    void finish(@NotNull GameContext ctx);

    /**
     * Called if the game is getting interrupted, this is called before players are getting kicked out of the game.
     *
     * @param ctx   game context
     * @param cause exception that caused the game to be interrupted, or {@code null} if the game didn't fail and was instead interrupted.
     */
    void interrupt(@NotNull GameContext ctx, @Nullable Throwable cause);

    /**
     * Here you should terminate any processes and clean up all resources the game is using.
     *
     * @param ctx game context.
     */
    void terminate(@NotNull GameContext ctx);
}
