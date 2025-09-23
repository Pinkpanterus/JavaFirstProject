package com.example.dungeon.model;

public class Monster extends Entity {
    private int level;
    private Item lootItem;

    public Monster(String name, int level, int hp, Item lootItem) {
        super(name, hp);
        this.level = level;
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
}
