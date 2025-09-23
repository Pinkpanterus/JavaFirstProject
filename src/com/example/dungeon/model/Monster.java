package com.example.dungeon.model;

public class Monster extends Entity {
    private int level;
    private int attackDamage;
    private Item lootItem;

    public Monster(String name, int level, int hp, int attackDamage, Item lootItem) {
        super(name, hp);
        this.level = level;
        this.attackDamage = attackDamage;
        this.lootItem = lootItem;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public Item getLootItem() {
        return lootItem;
    }

    public void setLootItem(Item lootItem) {
        this.lootItem = lootItem;
    }


    public int getAttackDamage() {
        return attackDamage;
    }

    public void setAttackDamage(int attackDamage) {
        this.attackDamage = attackDamage;
    }
}
