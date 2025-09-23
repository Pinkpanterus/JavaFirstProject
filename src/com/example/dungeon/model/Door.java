package com.example.dungeon.model;

public class Door extends Item {
    private boolean isLocked;
    private final Key openingKey;

    public Door(String name, boolean isLocked, Key openingKey) {
        super(name);
        this.isLocked = isLocked;
        this.openingKey = openingKey;
    }

    public boolean isLocked() {
        return this.isLocked;
    }

    public Key getOpenningKey() {
        return this.openingKey;
    }

    public void unlock() {
        this.isLocked = false;
    }

    @Override
    public void apply(GameState ctx) {
        this.isLocked = false;
        System.out.println("Дверь '" + getName() + "' открыта!");
    }
}