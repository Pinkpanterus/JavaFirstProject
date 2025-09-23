package com.example.dungeon.core;

import com.example.dungeon.model.*;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SaveLoad {
    private static final Path SAVE = Paths.get("save.txt");
    private static final Path SCORES = Paths.get("scores.csv");

    public static void save(GameState s, Map<String, Room> allRooms) {
        try (BufferedWriter w = Files.newBufferedWriter(SAVE,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {

            // Сохраняем данные игрока
            Player p = s.getPlayer();
            w.write("player:" + p.getName() + ":" + p.getHp() + ":" + p.getAttack());
            w.newLine();

            // Сохраняем инвентарь игрока
            if (!p.getInventory().isEmpty()) {
                String inventoryData = p.getInventory().stream()
                        .map(item -> item.getClass().getSimpleName() + "|" + item.getName() + "|" + getItemExtraData(item))
                        .collect(Collectors.joining(","));
                w.write("player_inventory:" + inventoryData);
                w.newLine();
            }

            // Сохраняем текущую комнату
            w.write("current_room:" + s.getCurrent().getName());
            w.newLine();

            // Сохраняем счет
            w.write("score:" + s.getScore());
            w.newLine();

            // Сохраняем все комнаты с их содержимым
            w.write("=== ROOMS DATA ===");
            w.newLine();

            for (Room room : allRooms.values()) {
                // Сохраняем информацию о комнате
                w.write("room:" + room.getName() + ":" + escapeColons(room.getDescription()));
                w.newLine();

                // Сохраняем предметы в комнате
                if (!room.getItems().isEmpty()) {
                    String roomItems = room.getItems().stream()
                            .map(item -> item.getClass().getSimpleName() + "|" + item.getName() + "|" + getItemExtraData(item))
                            .collect(Collectors.joining(","));
                    w.write("room_items:" + room.getName() + ":" + roomItems);
                    w.newLine();
                }

                // Сохраняем монстра в комнате
                if (room.getMonster() != null) {
                    Monster monster = room.getMonster();
                    String monsterData = monster.getName() + ":" + monster.getLevel() + ":" + monster.getHp();

                    // Сохраняем лут монстра
                    if (monster.getLootItem() != null) {
                        Item loot = monster.getLootItem();
                        monsterData += ":" + loot.getClass().getSimpleName() + "|" + loot.getName() + "|" + getItemExtraData(loot);
                    }

                    w.write("room_monster:" + room.getName() + ":" + monsterData);
                    w.newLine();
                }

                // Сохраняем связи между комнатами
                if (!room.getNeighbors().isEmpty()) {
                    String neighbors = room.getNeighbors().entrySet().stream()
                            .map(entry -> entry.getKey() + "=" + entry.getValue().getName())
                            .collect(Collectors.joining(","));
                    w.write("room_neighbors:" + room.getName() + ":" + neighbors);
                    w.newLine();
                }

                // Сохраняем двери в комнате
                if (!room.getDoors().isEmpty()) {
                    String doorsData = room.getDoors().entrySet().stream()
                            .map(entry -> {
                                Door door = entry.getValue();
                                String doorInfo = entry.getKey() + ">" +
                                        door.getName() + ">" +
                                        door.isLocked();

                                // Сохраняем ключ для двери, если он есть
                                if (door.getOpenningKey() != null) {
                                    Key key = door.getOpenningKey();
                                    doorInfo += ">" + key.getClass().getSimpleName() + "|" + key.getName();
                                } else {
                                    doorInfo += ">null";
                                }

                                return doorInfo;
                            })
                            .collect(Collectors.joining(","));
                    w.write("room_doors:" + room.getName() + ":" + doorsData);
                    w.newLine();
                }
            }

            System.out.println("Игра сохранена в " + SAVE.toAbsolutePath());
            writeScore(p.getName(), s.getScore());

        } catch (IOException e) {
            throw new UncheckedIOException("Не удалось сохранить игру: " + e.getMessage(), e);
        }
    }

    // Вспомогательный метод для экранирования двоеточий в описании
    private static String escapeColons(String text) {
        return text.replace(":", "\\:");
    }

    // Вспомогательный метод для получения дополнительных данных предмета
    private static String getItemExtraData(Item item) {
        if (item instanceof Potion potion) {
            return String.valueOf(potion.getHealAmount());
        } else if (item instanceof Weapon weapon) {
            return String.valueOf(weapon.getDamageBonus());
        }
        return "0";
    }

    public static void load(GameState s, Function<String, Room> roomFinder) {
        if (!Files.exists(SAVE)) {
            System.out.println("Сохранение не найдено.");
            return;
        }

        try (BufferedReader r = Files.newBufferedReader(SAVE)) {
            Map<String, String> data = new HashMap<>();
            Map<String, List<String>> roomItems = new HashMap<>();
            Map<String, String> roomMonsters = new HashMap<>();
            Map<String, String> roomNeighbors = new HashMap<>();
            Map<String, String> roomDoors = new HashMap<>();
            String line;
            boolean inRoomsSection = false;

            while ((line = r.readLine()) != null) {
                if (line.equals("=== ROOMS DATA ===")) {
                    inRoomsSection = true;
                    continue;
                }

                if (!inRoomsSection) {
                    // Данные игрока и общие данные
                    String[] parts = line.split(":", 2);
                    if (parts.length == 2) {
                        data.put(parts[0], parts[1]);
                    }
                } else {
                    // Данные комнат
                    String[] parts = line.split(":", 3);
                    if (parts.length >= 3) {
                        switch (parts[0]) {
                            case "room" -> data.put("room_" + parts[1], unescapeColons(parts[2])); // Описание комнаты
                            case "room_items" -> roomItems.put(parts[1], Arrays.asList(parts[2].split(",")));
                            case "room_monster" -> roomMonsters.put(parts[1], parts[2]);
                            case "room_neighbors" -> roomNeighbors.put(parts[1], parts[2]);
                            case "room_doors" -> roomDoors.put(parts[1], parts[2]);
                        }
                    }
                }
            }

            // Загружаем данные игрока
            if (data.containsKey("player")) {
                String[] playerData = data.get("player").split(":");
                if (playerData.length >= 3) {
                    Player p = s.getPlayer();
                    p.setName(playerData[0]);
                    p.setHp(Integer.parseInt(playerData[1]));
                    p.setAttack(Integer.parseInt(playerData[2]));
                }
            }

            // Загружаем инвентарь игрока
            if (data.containsKey("player_inventory")) {
                Player p = s.getPlayer();
                p.getInventory().clear();

                String[] inventoryItems = data.get("player_inventory").split(",");
                for (String itemData : inventoryItems) {
                    String[] itemParts = itemData.split("\\|");
                    if (itemParts.length >= 2) {
                        Item item = createItemFromData(itemParts);
                        if (item != null) {
                            p.getInventory().add(item);
                        }
                    }
                }
            }

            // Загружаем текущую комнату и восстанавливаем её состояние
            if (data.containsKey("current_room")) {
                String roomName = data.get("current_room");
                Room targetRoom = roomFinder.apply(roomName);
                if (targetRoom != null) {
                    s.setCurrent(targetRoom);

                    // Восстанавливаем предметы в комнате
                    if (roomItems.containsKey(roomName)) {
                        targetRoom.getItems().clear();
                        for (String itemData : roomItems.get(roomName)) {
                            String[] itemParts = itemData.split("\\|");
                            if (itemParts.length >= 2) {
                                Item item = createItemFromData(itemParts);
                                if (item != null) {
                                    targetRoom.getItems().add(item);
                                }
                            }
                        }
                    }

                    // Восстанавливаем монстра в комнате
                    if (roomMonsters.containsKey(roomName)) {
                        String[] monsterData = roomMonsters.get(roomName).split(":");
                        if (monsterData.length >= 3) {
                            // Создаем монстра
                            Monster monster = new Monster(
                                    monsterData[0],
                                    Integer.parseInt(monsterData[1]),
                                    Integer.parseInt(monsterData[2]),
                                    null
                            );

                            // Восстанавливаем лут монстра, если он есть
                            if (monsterData.length >= 4) {
                                String[] lootParts = monsterData[3].split("\\|");
                                if (lootParts.length >= 2) {
                                    Item lootItem = createItemFromData(lootParts);
                                    // Для установки лута нужен сеттер, добавим его временно через рефлексию
                                    try {
                                        var lootField = Monster.class.getDeclaredField("lootItem");
                                        lootField.setAccessible(true);
                                        lootField.set(monster, lootItem);
                                    } catch (Exception e) {
                                        System.out.println("Не удалось установить лут монстра: " + e.getMessage());
                                    }
                                }
                            }

                            targetRoom.setMonster(monster);
                        }
                    }

                    // Восстанавливаем связи между комнатами
                    if (roomNeighbors.containsKey(roomName)) {
                        targetRoom.getNeighbors().clear();
                        String[] neighborsData = roomNeighbors.get(roomName).split(",");
                        for (String neighborData : neighborsData) {
                            String[] parts = neighborData.split("=");
                            if (parts.length == 2) {
                                Room neighborRoom = roomFinder.apply(parts[1]);
                                if (neighborRoom != null) {
                                    targetRoom.getNeighbors().put(parts[0], neighborRoom);
                                }
                            }
                        }
                    }

                    // Восстанавливаем двери в комнате
                    if (roomDoors.containsKey(roomName)) {
                        targetRoom.getDoors().clear();
                        String[] doorsData = roomDoors.get(roomName).split(",");
                        for (String doorData : doorsData) {
                            String[] parts = doorData.split(">");
                            if (parts.length >= 3) {
                                String direction = parts[0];
                                String doorName = parts[1];
                                boolean isLocked = Boolean.parseBoolean(parts[2]);

                                Key openingKey = null;
                                if (parts.length >= 4 && !"null".equals(parts[3])) {
                                    String[] keyParts = parts[3].split("\\|");
                                    if (keyParts.length >= 2) {
                                        openingKey = new Key(keyParts[1]);
                                    }
                                }

                                Door door = new Door(doorName, isLocked, openingKey);
                                targetRoom.getDoors().put(direction, door);
                            }
                        }
                    }
                }
            }

            // Загружаем счет
            if (data.containsKey("score")) {
                try {
                    s.addScore(Integer.parseInt(data.get("score")));
                } catch (NumberFormatException e) {
                    System.out.println("Ошибка загрузки счета: " + e.getMessage());
                }
            }

            System.out.println("Игра успешно загружена");

        } catch (IOException e) {
            throw new UncheckedIOException("Не удалось загрузить игру: " + e.getMessage(), e);
        } catch (NumberFormatException e) {
            System.out.println("Ошибка формата числовых данных в сохранении: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Неожиданная ошибка при загрузке: " + e.getMessage());
        }
    }

    // Вспомогательный метод для восстановления текста
    private static String unescapeColons(String text) {
        return text.replace("\\:", ":");
    }

    private static Item createItemFromData(String[] itemParts) {
        String itemType = itemParts[0];
        String itemName = itemParts[1];
        int extraData = itemParts.length > 2 ? Integer.parseInt(itemParts[2]) : 0;

        return switch (itemType) {
            case "Potion" -> new Potion(itemName, extraData);
            case "Weapon" -> new Weapon(itemName, extraData);
            case "Key" -> new Key(itemName);
            default -> {
                // Создаем анонимный предмет для неизвестных типов
                yield new Item(itemName) {
                    @Override
                    public void apply(GameState ctx) {
                        System.out.println("Предмет " + itemName + " не имеет специального эффекта.");
                    }
                };
            }
        };
    }

    public static void printScores() {
        if (!Files.exists(SCORES)) {
            System.out.println("Пока нет результатов.");
            return;
        }
        try (BufferedReader r = Files.newBufferedReader(SCORES)) {
            System.out.println("Таблица лидеров (топ-10):");
            r.lines().skip(1).map(l -> l.split(","))
                    .filter(parts -> parts.length >= 3)
                    .map(a -> new Score(a[1], Integer.parseInt(a[2])))
                    .sorted(Comparator.comparingInt(Score::score).reversed())
                    .limit(10)
                    .forEach(s -> System.out.println(s.player() + " — " + s.score()));
        } catch (IOException e) {
            System.err.println("Ошибка чтения результатов: " + e.getMessage());
        }
    }

    private static void writeScore(String player, int score) {
        try {
            boolean header = !Files.exists(SCORES);
            try (BufferedWriter w = Files.newBufferedWriter(SCORES, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
                if (header) {
                    w.write("timestamp,player,score");
                    w.newLine();
                }
                w.write(LocalDateTime.now() + "," + player + "," + score);
                w.newLine();
            }
        } catch (IOException e) {
            System.err.println("Не удалось записать очки: " + e.getMessage());
        }
    }

    private record Score(String player, int score) {
    }
}