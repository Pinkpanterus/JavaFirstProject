package com.example.dungeon.model;

public class Door extends Item {
    private boolean isLocked;
    private final Key openningKey;

    public Door(String name, boolean isLocked, Key openningKey) {
        super(name);
        this.isLocked = isLocked;
        this.openningKey = openningKey;
    }

    public boolean isLocked() {
        return this.isLocked;
    }

    public Key getOpenningKey() {
        return this.openningKey;
    }

    @Override
    public void apply(GameState ctx) {
        this.isLocked = false;
    }
}
