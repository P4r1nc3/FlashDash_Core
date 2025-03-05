package com.flashdash.utils;

public enum ResourceType {
    DECK("deck"),
    USER("user"),
    GAME_SESSION("game-session"),
    QUESTION("question"),
    INVITATION("invitation");

    private final String type;

    ResourceType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
