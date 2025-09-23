package com.example.dungeon.model;

import java.util.*;

public class Room {
    private final String name;
    private final String description;
    private final Map<String, Room> neighbors = new HashMap<>();
    private final Map<String, Door> doors = new HashMap<>();
    private final List<Item> items = new ArrayList<>();
    private Monster monster;

    public Room(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public Map<String, Room> getNeighbors() {
        return neighbors;
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

    public Map<String, Door> getDoors() {return this.doors;}
    public Door getDoorByDirection(String direction){return this.doors.get(direction);}

    public void setDoor(String direction, Door door){this.doors.put(direction, door); }

    public String getDescription() {
        StringBuilder sb = new StringBuilder(name + ": " + description);
        if (!items.isEmpty()) {
            sb.append("\nПредметы: ").append(String.join(", ", items.stream().map(Item::getName).toList()));
        }
        if (monster != null) {
            sb.append("\nВ комнате монстр: ").append(monster.getName()).append(" (ур. ").append(monster.getLevel()).append(")");
        }
        if (!neighbors.isEmpty()) {
            sb.append("\nВыходы: ").append(String.join(", ", neighbors.keySet()));
        }
        //Закрытые двери
        var closedDoors = doors.entrySet()
                .stream()
                .filter(e -> e.getValue().isLocked());
        if (closedDoors.findAny().isPresent()) {
        sb.append("\nЗакрытые двери: ").append(String.join(", ", doors.keySet()));
//        doors.entrySet()
//                .stream()
//                .filter(e -> e.getValue().isLocked())
//                .forEach(e -> sb.append('\n')
//                        .append(e.getValue().getName()) // название двери
//                        .append(" по направлению ")
//                        .append(e.getKey()));
        }     // direction из Map

        return sb.toString();
    }
}
