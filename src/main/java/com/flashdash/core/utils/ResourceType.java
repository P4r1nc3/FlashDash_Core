package com.flashdash.core.utils;

public enum ResourceType {
    DECK("deck"),
    USER("user"),
    GAME_SESSION("game-session"),
    QUESTION("question"),
    INVITATION("invitation"),
    ACTIVITY("activity");

    private final String type;

    ResourceType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
