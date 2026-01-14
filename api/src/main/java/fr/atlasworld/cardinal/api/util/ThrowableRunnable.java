package fr.atlasworld.cardinal.api.util;

/**
 * Special version of java {@link Runnable} interface but allows for any exception to be thrown inside it.
 */
@FunctionalInterface
public interface ThrowableRunnable {

    /**
     * Runs this operation.
     *
     * @throws Throwable if operation fails.
     */
    void run() throws Throwable;
}
