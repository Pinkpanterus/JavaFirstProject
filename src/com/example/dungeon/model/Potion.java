package com.example.dungeon.model;

public class Potion extends Item {
    private final int heal;

    public Potion(String name, int heal) {
        super(name);
        this.heal = heal;
    }

    public int getHealAmount() {
        return heal;
    }

    @Override
    public void apply(GameState ctx) {
        Player p = ctx.getPlayer();
        p.setMaxHP(p.getMaxHP() + heal);
        System.out.println("Выпито зелье: +" + heal + " HP. Текущее HP: " + p.getMaxHP());
        p.getInventory().remove(this);
    }
}
