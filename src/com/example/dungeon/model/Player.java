package com.example.dungeon.model;

import java.util.*;

public class Player extends Entity {
    private int attackDamage;
    private final List<Item> inventory = new ArrayList<>();

    public Player(String name, int hp, int attack) {
        super(name, hp);
        this.attackDamage = attack;
    }

    public int getAttackDamage() {
        return attackDamage;
    }

    public void setAttackDamage(int attackDamage) {
        this.attackDamage = attackDamage;
    }

    public List<Item> getInventory() {
        return inventory;
    }
}
