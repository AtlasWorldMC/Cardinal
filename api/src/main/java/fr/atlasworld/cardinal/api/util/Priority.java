package fr.atlasworld.cardinal.api.util;

public enum Priority {
    HIGHEST(1000),
    HIGH(750),
    NORMAL(500),
    LOW(250),
    LOWEST(0);

    private final int priority;

    Priority(int priority) {
        this.priority = priority;
    }

    public int priority() {
        return this.priority;
    }
}
