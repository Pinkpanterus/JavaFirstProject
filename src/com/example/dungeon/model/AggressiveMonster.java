package com.example.dungeon.model;

public class AggressiveMonster extends Monster {
    public AggressiveMonster(String name, int level, int hp, int attackDamage, Item lootItem) {
        super(name, level, hp, attackDamage, lootItem);
    }

    public boolean tryAttack(Player player) {
        // 50% шанс атаки вместо 30%
        if (Math.random() < 0.5) {
            int damage = getAttackDamage(); // +1 урон
            player.setCurrentHP(player.getCurrentHP() - damage);
            System.out.println("💢 " + getName() + " яростно атакует на " + damage + " урона!");
            return true;
        }
        return false;
    }
}