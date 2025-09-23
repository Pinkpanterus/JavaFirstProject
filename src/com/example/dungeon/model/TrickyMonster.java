package com.example.dungeon.model;

public class TrickyMonster extends Monster {
    public TrickyMonster(String name, int level, int hp, int attackDamage, Item lootItem) {
        super(name, level, hp, attackDamage, lootItem);
    }

    public boolean tryDodge() {
        // 25% шанс уклониться от атаки
        return Math.random() < 0.25;
    }
}
