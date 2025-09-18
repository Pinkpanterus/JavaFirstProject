package com.example.dungeon.core;

import com.example.dungeon.model.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

public class Game {
    private final GameState state = new GameState();
    private final Map<String, Command> commands = new LinkedHashMap<>();

    static {
        WorldInfo.touch("Game");
    }

    public Game() {
        registerCommands();
        bootstrapWorld();
    }

    private void registerCommands() {
        commands.put("help", (ctx, a) -> System.out.println("Команды: " + String.join(", ", commands.keySet())));
        commands.put("gc-stats", (ctx, a) -> {
//            Runtime rt = Runtime.getRuntime();
//            long free = rt.freeMemory(), total = rt.totalMemory(), used = total - free;
//            System.out.println("Память: used=" + used + " free=" + free + " total=" + total);

            Runtime rt = Runtime.getRuntime();
            long free = rt.freeMemory(), total = rt.totalMemory(), used = total - free;
            long max = rt.maxMemory();

            System.out.println("Память JVM:");
            System.out.println("  Used: " + (used / 1024 / 1024) + " MB");
            System.out.println("  Free: " + (free / 1024 / 1024) + " MB");
            System.out.println("  Total: " + (total / 1024 / 1024) + " MB");
            System.out.println("  Max: " + (max / 1024 / 1024) + " MB");

            // Демонстрация работы GC
            System.out.println("Запуск GC...");
            long before = rt.totalMemory() - rt.freeMemory();
            System.gc();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            long after = rt.totalMemory() - rt.freeMemory();
            System.out.println("Освобождено памяти: " + ((before - after) / 1024) + " KB");
        });
        commands.put("look", (ctx, a) -> System.out.println(ctx.getCurrent().describe()));
        commands.put("move", (ctx, a) -> {
            if (a.isEmpty()) {
                throw new InvalidCommandException("Укажите направление: north, south, east, west");
            }

            String direction = a.getFirst().toLowerCase();
            Room currentRoom = ctx.getCurrent();
            Room nextRoom = currentRoom.getNeighbors().get(direction);

            if (nextRoom == null) {
                throw new InvalidCommandException("Нет выхода в направлении: " + direction);
            }

            ctx.setCurrent(nextRoom);
            System.out.println("Вы перешли в: " + nextRoom.getName());
            System.out.println(nextRoom.describe());
        });
        commands.put("take", (ctx, a) -> {
            if (a.isEmpty()) {
                throw new InvalidCommandException("Укажите название предмета");
            }

            String itemName = String.join(" ", a);
            Room currentRoom = ctx.getCurrent();
            Player player = ctx.getPlayer();

            // Находим предмет в комнате
            Item foundItem = currentRoom.getItems().stream()
                .filter(item -> item.getName().equalsIgnoreCase(itemName))
                .findFirst()
                .orElse(null);

            if (foundItem == null) {
                throw new InvalidCommandException("Предмет '" + itemName + "' не найден в комнате");
            }

            // Переносим предмет в инвентарь
            currentRoom.getItems().remove(foundItem);
            player.getInventory().add(foundItem);
            System.out.println("Взято: " + foundItem.getName());
        });
        commands.put("inventory", (ctx, a) -> {
            Player player = ctx.getPlayer();
            List<Item> inventory = player.getInventory();

            if (inventory.isEmpty()) {
                System.out.println("Инвентарь пуст");
                return;
            }

            inventory.stream()
                    .collect(Collectors.groupingBy(
                            item -> item.getClass().getSimpleName(),
                            Collectors.counting()
                    ))
                    .entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(entry -> {
                        String type = entry.getKey();
                        long count = entry.getValue();

                        String itemsList = inventory.stream()
                                .filter(item -> item.getClass().getSimpleName().equals(type))
                                .map(Item::getName)
                                .sorted()
                                .collect(Collectors.joining(", "));

                        System.out.println("- " + type + " (" + count + "): " + itemsList);
                    });
        });
        commands.put("use", (ctx, a) -> {
            if (a.isEmpty()) {
                throw new InvalidCommandException("Укажите название предмета");
            }

            String itemName = String.join(" ", a);
            Player player = ctx.getPlayer();

            Item foundItem = player.getInventory().stream()
                    .filter(item -> item.getName().equalsIgnoreCase(itemName))
                    .findFirst()
                    .orElse(null);

            if (foundItem == null) {
                throw new InvalidCommandException("Предмет '" + itemName + "' не найден в инвентаре");
            }

            // Применяем предмет
            foundItem.apply(ctx);
        });
        commands.put("fight", (ctx, a) -> {
            Room currentRoom = ctx.getCurrent();
            Player player = ctx.getPlayer();
            Monster monster = currentRoom.getMonster();

            if (monster == null) {
                throw new InvalidCommandException("В этой комнате нет монстров для боя");
            }

            System.out.println("Начинается бой с " + monster.getName() + "!");

            // Пошаговый бой
            while (player.getHp() > 0 && monster.getHp() > 0) {
                // Игрок атакует
                int playerDamage = player.getAttack();
                monster.setHp(monster.getHp() - playerDamage);
                System.out.println("Вы бьёте " + monster.getName() + " на " + playerDamage +
                        ". HP монстра: " + Math.max(0, monster.getHp()));

                if (monster.getHp() <= 0) {
                    System.out.println("Вы победили " + monster.getName() + "!");

                    if (Math.random() < 0.5) { // 50% шанс выпадения лута
                        Potion loot = new Potion("Зелье здоровья", 10);
                        currentRoom.getItems().add(loot);
                        System.out.println(monster.getName() + " выпало: " + loot.getName());
                    }

                    currentRoom.setMonster(null);
                    ctx.addScore(10); // Бонус за победу
                    return;
                }

                // Монстр атакует
                int monsterDamage = monster.getLevel();
                player.setHp(player.getHp() - monsterDamage);
                System.out.println(monster.getName() + " отвечает на " + monsterDamage +
                        ". Ваше HP: " + Math.max(0, player.getHp()));

                if (player.getHp() <= 0) {
                    System.out.println("Вы погибли! Игра окончена.");
                    System.exit(0);
                }

                // Пауза между раундами
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        commands.put("save", (ctx, a) -> SaveLoad.save(ctx));
        commands.put("load", (ctx, a) -> SaveLoad.load(ctx));
        commands.put("scores", (ctx, a) -> SaveLoad.printScores());
        commands.put("exit", (ctx, a) -> {
            System.out.println("Пока!");
            System.exit(0);
        });
        commands.put("about", (ctx, a) -> {
            System.out.println("DungeonMini v1.0");
            System.out.println("Разработчик: Гроностайский А.И.");
            System.out.println("Java version: " + System.getProperty("java.version"));
        });
    }

    private void bootstrapWorld() {
        Player hero = new Player("Герой", 20, 5);
        state.setPlayer(hero);

        Room square = new Room("Площадь", "Каменная площадь с фонтаном.");
        Room forest = new Room("Лес", "Шелест листвы и птичий щебет.");
        Room cave = new Room("Пещера", "Темно и сыро.");
        square.getNeighbors().put("north", forest);
        forest.getNeighbors().put("south", square);
        forest.getNeighbors().put("east", cave);
        cave.getNeighbors().put("west", forest);

        forest.getItems().add(new Potion("Малое зелье", 5));
        forest.setMonster(new Monster("Волк", 1, 8));

        state.setCurrent(square);
    }

    public void run() {
        System.out.println("DungeonMini (TEMPLATE). 'help' — команды.");
        try (BufferedReader in = new BufferedReader(new InputStreamReader(System.in))) {
            while (true) {
                System.out.print("> ");
                String line = in.readLine();
                if (line == null) break;
                line = line.trim();
                if (line.isEmpty()) continue;
                List<String> parts = Arrays.asList(line.split("\s+"));
                String cmd = parts.getFirst().toLowerCase(Locale.ROOT);
                List<String> args = parts.subList(1, parts.size());
                Command c = commands.get(cmd);
                try {
                    if (c == null) throw new InvalidCommandException("Неизвестная команда: " + cmd);
                    c.execute(state, args);
                    state.addScore(1);
                } catch (InvalidCommandException e) {
                    System.out.println("Ошибка: " + e.getMessage());
                } catch (Exception e) {
                    System.out.println("Непредвиденная ошибка: " + e.getClass().getSimpleName() + ": " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.out.println("Ошибка ввода/вывода: " + e.getMessage());
        }
    }
}
