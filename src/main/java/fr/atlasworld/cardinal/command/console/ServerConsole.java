package fr.atlasworld.cardinal.command.console;

import fr.atlasworld.cardinal.bootstrap.Main;
import net.minecrell.terminalconsole.SimpleTerminalConsole;
import net.minestom.server.MinecraftServer;

public final class ServerConsole extends SimpleTerminalConsole {
    private boolean running;

    @Override
    public void start() {
        this.running = true;
        super.start();
    }

    public void stop() {
        this.running = false;
    }

    @Override
    protected boolean isRunning() {
        return this.running;
    }

    @Override
    protected void runCommand(String command) {
        MinecraftServer.getCommandManager().execute(MinecraftServer.getCommandManager().getConsoleSender(), command);
    }

    /*
    * Depending on which environment the console is running,
    * when shutting the server, JLine may throw a UserInterruptException, and TerminalConsole will call this method as a result,
    * to prevent the server from trying to shut down twice; we early return if the console stopped running.
    */
    @Override
    protected void shutdown() {
        if (!this.running)
            return;

        MinecraftServer.getSchedulerManager().scheduleEndOfTick(Main::shutdown);
    }
}
