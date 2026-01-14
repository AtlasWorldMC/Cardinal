package fr.atlasworld.cardinal.command.console;

public final class ConsoleThread extends Thread {
    private final ServerConsole console;

    public ConsoleThread() {
        this.console = new ServerConsole();

        this.setPriority(MIN_PRIORITY);
        this.setName("server-console");
    }

    @Override
    public void run() {
        this.console.start(); // Will block
    }

    public void shutdown() {
        this.console.stop();
        this.interrupt();
    }
}
