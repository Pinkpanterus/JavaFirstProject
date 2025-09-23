package com.example.dungeon.model;

public abstract class Entity {
    private String name;
    private int maxHP;
    private int currentHP;

    public Entity(String name, int hp) {
        this.name = name;
        this.maxHP = hp;
        this.currentHP = hp;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMaxHP() {
        return maxHP;
    }

    public void setMaxHP(int maxHP) {
        this.maxHP = maxHP;
    }

    public int getCurrentHP() {
        return currentHP;
    }

    public void setCurrentHP(int currentHP) {
        this.currentHP = currentHP;
    }
}
