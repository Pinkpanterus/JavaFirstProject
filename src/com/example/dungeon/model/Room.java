package com.example.dungeon.model;

import java.util.*;
import java.util.stream.Collectors;

public class Room {
    private final String name;
    private final String description;
    private final Map<String, Way> ways = new HashMap<>(); // Заменяем neighbors и doors
    private final List<Item> items = new ArrayList<>();
    private Monster monster;

    public Room(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public Map<String, Way> getWays() {
        return ways;
    }

    public List<Item> getItems() {
        return items;
    }

    public Monster getMonster() {
        return monster;
    }

    public void setMonster(Monster m) {
        this.monster = m;
    }

    // Метод для добавления прохода без двери
    public void addWay(String direction, Room room) {
        ways.put(direction, new Way(room));
    }

    // Метод для добавления прохода с дверью
    public void addWayWithDoor(String direction, Room room, Door door) {
        ways.put(direction, new Way(room, door));
    }

    // Метод для получения Way по направлению
    public Way getWay(String direction) {
        return ways.get(direction);
    }

    // Метод для получения двери по направлению
    public Door getDoor(String direction) {
        Way way = ways.get(direction);
        return way != null ? way.getDoor() : null;
    }

    public String getDescription() {
        StringBuilder sb = new StringBuilder(name + ": " + description);

        if (!items.isEmpty()) {
            sb.append("\nПредметы: ").append(items.stream().map(Item::getName).collect(Collectors.joining(", ")));
        }

        if (monster != null) {
            sb.append("\nВ комнате монстр: ").append(monster.getName()).append(" (ур. ").append(monster.getLevel()).append(")");
        }

        if (!ways.isEmpty()) {
            sb.append("\nВыходы: ");
            List<String> exits = new ArrayList<>();
            for (Map.Entry<String, Way> entry : ways.entrySet()) {
                String direction = entry.getKey();
                Way way = entry.getValue();
                boolean hasDoor = way.getDoor() != null;
                exits.add(hasDoor? direction + " - " + way.getDescription(): direction);
            }
            sb.append(String.join(", ", exits));
        }

        return sb.toString();
    }

    // Метод для проверки, можно ли пройти в указанном направлении
    public boolean canMove(String direction) {
        Way way = ways.get(direction);
        return way != null && !way.isBlocked();
    }

    // Метод для получения комнаты через проход
    public Room getRoomThroughWay(String direction) {
        Way way = ways.get(direction);
        return way != null ? way.getDestination() : null;
    }

    // Метод для попытки открыть дверь
    public String tryOpenDoor(String direction, Player player) {
        Way way = ways.get(direction);
        if (way == null) {
            return "Нет выхода в этом направлении";
        }

        Door door = way.getDoor();
        if (door == null) {
            return "В этом направлении нет двери";
        }

        if (!door.isLocked()) {
            return "Дверь уже открыта";
        }

        if (door.getOpenningKey() == null) {
            return "Дверь заперта, но нет информации о ключе";
        }

        boolean hasKey = player.getInventory().stream()
                .anyMatch(item -> item instanceof Key &&
                        item.getName().equals(door.getOpenningKey().getName()));

        if (hasKey) {
            door.apply(null); // Разблокируем дверь
            return "Дверь открыта ключом: " + door.getOpenningKey().getName();
        } else {
            return "Нужен ключ: " + door.getOpenningKey().getName();
        }
    }

    public boolean checkMonsterAttack(Player player) {
        if (monster != null && monster.getCurrentHP() > 0) {
            if (monster instanceof AggressiveMonster aggressiveMonster) {
                return aggressiveMonster.tryAttack(player);
            } else {
                // Стандартный шанс атаки - 30%
                if (Math.random() < 0.3) {
                    int monsterDamage = monster.getAttackDamage();
                    player.setCurrentHP(player.getCurrentHP() - monsterDamage);
                    System.out.println("⚡ " + monster.getName() + " атакует вас на " + monsterDamage + " урона!");
                    System.out.println("Ваше HP: " + player.getCurrentHP());
                    return true;
                }
            }
        }
        return false;
    }

    public void monsterAmbush(Player player) {
        if (monster != null && monster.getCurrentHP() > 0) {
            int monsterDamage = monster.getLevel() + 2; // Засада сильнее
            player.setMaxHP(player.getMaxHP() - monsterDamage);
            System.out.println("🎯 " + monster.getName() + " устраивает засаду! Урон: " + monsterDamage);
            System.out.println("Ваше HP: " + player.getMaxHP());
        }
    }


}