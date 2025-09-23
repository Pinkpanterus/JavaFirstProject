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

            // Сохраняем данные игрока (текущее HP вместо максимального)
            Player p = s.getPlayer();
            w.write("player:" + p.getName() + ":" + p.getCurrentHP() + ":" + p.getMaxHP() + ":" + p.getAttackDamage());
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
            w.write("current_room:" + s.getCurrentRoom().getName());
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

                // Сохраняем монстра в комнате (текущее HP вместо максимального)
                if (room.getMonster() != null) {
                    Monster monster = room.getMonster();
                    // Сохраняем тип монстра для корректного восстановления
                    String monsterType = monster.getClass().getSimpleName();
                    String monsterData = monsterType + ":" + monster.getName() + ":" + monster.getLevel() + ":" +
                            monster.getCurrentHP() + ":" + monster.getMaxHP() + ":" + monster.getAttackDamage();

                    // Сохраняем лут монстра
                    if (monster.getLootItem() != null) {
                        Item loot = monster.getLootItem();
                        monsterData += ":" + loot.getClass().getSimpleName() + "|" + loot.getName() + "|" + getItemExtraData(loot);
                    }

                    w.write("room_monster:" + room.getName() + ":" + monsterData);
                    w.newLine();
                }

                // Сохраняем пути (ways) из комнаты
                if (!room.getWays().isEmpty()) {
                    String waysData = room.getWays().entrySet().stream()
                            .map(entry -> {
                                String direction = entry.getKey();
                                Way way = entry.getValue();
                                String wayInfo = direction + ">" + way.getDestination().getName();

                                // Сохраняем информацию о двери, если она есть
                                if (way.getDoor() != null) {
                                    Door door = way.getDoor();
                                    wayInfo += ">" + door.getName() + ">" + door.isLocked();

                                    if (door.getOpenningKey() != null) {
                                        Key key = door.getOpenningKey();
                                        wayInfo += ">" + key.getClass().getSimpleName() + "|" + key.getName();
                                    } else {
                                        wayInfo += ">null";
                                    }
                                } else {
                                    wayInfo += ">null>null>null"; // Нет двери
                                }

                                return wayInfo;
                            })
                            .collect(Collectors.joining(","));
                    w.write("room_ways:" + room.getName() + ":" + waysData);
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
            Map<String, String> roomWays = new HashMap<>();
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
                            case "room" -> data.put("room_" + parts[1], unescapeColons(parts[2]));
                            case "room_items" -> roomItems.put(parts[1], Arrays.asList(parts[2].split(",")));
                            case "room_monster" -> roomMonsters.put(parts[1], parts[2]);
                            case "room_ways" -> roomWays.put(parts[1], parts[2]);
                        }
                    }
                }
            }

            // Загружаем данные игрока
            if (data.containsKey("player")) {
                String[] playerData = data.get("player").split(":");
                if (playerData.length >= 4) {
                    Player p = s.getPlayer();
                    p.setName(playerData[0]);
                    p.setCurrentHP(Integer.parseInt(playerData[1])); // Текущее HP
                    p.setMaxHP(Integer.parseInt(playerData[2])); // Максимальное HP
                    p.setAttackDamage(Integer.parseInt(playerData[3]));
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
                    s.setCurrentRoom(targetRoom);

                    // Восстанавливаем предметы в комнате
                    restoreRoomItems(targetRoom, roomItems);

                    // Восстанавливаем монстра в комнате
                    restoreRoomMonster(targetRoom, roomMonsters, roomFinder);

                    // Восстанавливаем пути из комнаты
                    restoreRoomWays(targetRoom, roomWays, roomFinder);
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

    // Восстановление предметов в комнате
    private static void restoreRoomItems(Room room, Map<String, List<String>> roomItems) {
        String roomName = room.getName();
        if (roomItems.containsKey(roomName)) {
            room.getItems().clear();
            for (String itemData : roomItems.get(roomName)) {
                String[] itemParts = itemData.split("\\|");
                if (itemParts.length >= 2) {
                    Item item = createItemFromData(itemParts);
                    if (item != null) {
                        room.getItems().add(item);
                    }
                }
            }
        }
    }

    // Восстановление монстра в комнате
    private static void restoreRoomMonster(Room room, Map<String, String> roomMonsters, Function<String, Room> roomFinder) {
        String roomName = room.getName();
        if (roomMonsters.containsKey(roomName)) {
            String[] monsterData = roomMonsters.get(roomName).split(":");
            if (monsterData.length >= 6) {
                String monsterType = monsterData[0];
                String monsterName = monsterData[1];
                int monsterLevel = Integer.parseInt(monsterData[2]);
                int monsterCurrentHP = Integer.parseInt(monsterData[3]); // Текущее HP
                int monsterMaxHP = Integer.parseInt(monsterData[4]); // Максимальное HP
                int monsterAttack = Integer.parseInt(monsterData[5]);

                // Создаем монстра соответствующего типа
                Monster monster;
                switch (monsterType) {
                    case "AggressiveMonster":
                        monster = new AggressiveMonster(monsterName, monsterLevel, monsterMaxHP, monsterAttack, null);
                        break;
                    case "TrickyMonster":
                        monster = new TrickyMonster(monsterName, monsterLevel, monsterMaxHP, monsterAttack, null);
                        break;
                    case "Monster":
                    default:
                        monster = new Monster(monsterName, monsterLevel, monsterMaxHP, monsterAttack, null);
                        break;
                }

                // Устанавливаем текущее HP монстра
                monster.setCurrentHP(monsterCurrentHP);

                // Восстанавливаем лут монстра, если он есть
                if (monsterData.length >= 7) {
                    String[] lootParts = monsterData[6].split("\\|");
                    if (lootParts.length >= 2) {
                        Item lootItem = createItemFromData(lootParts);
                        monster.setLootItem(lootItem);
                    }
                }

                room.setMonster(monster);
            }
        }
    }

    // Восстановление путей из комнаты
    private static void restoreRoomWays(Room room, Map<String, String> roomWays, Function<String, Room> roomFinder) {
        String roomName = room.getName();
        if (roomWays.containsKey(roomName)) {
            room.getWays().clear();
            String[] waysData = roomWays.get(roomName).split(",");
            for (String wayInfo : waysData) {
                String[] wayParts = wayInfo.split(">");
                if (wayParts.length >= 2) {
                    String direction = wayParts[0];
                    Room destRoom = roomFinder.apply(wayParts[1]);

                    if (destRoom != null) {
                        // Проверяем, есть ли дверь
                        if (wayParts.length >= 4 && !"null".equals(wayParts[2])) {
                            String doorName = wayParts[2];
                            boolean isLocked = Boolean.parseBoolean(wayParts[3]);
                            Key openingKey = null;

                            if (wayParts.length >= 5 && !"null".equals(wayParts[4])) {
                                String[] keyParts = wayParts[4].split("\\|");
                                if (keyParts.length >= 2) {
                                    openingKey = new Key(keyParts[1]);
                                }
                            }

                            Door door = new Door(doorName, isLocked, openingKey);
                            room.addWayWithDoor(direction, destRoom, door);
                        } else {
                            room.addWay(direction, destRoom);
                        }
                    }
                }
            }
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
            case "Door" -> {
                // Для обратной совместимости с старыми сохранениями
                boolean isLocked = extraData > 0;
                yield new Door(itemName, isLocked, null);
            }
            default -> {
                // Создаем анонимный предмет для неизвестных типов
                System.out.println("Создан предмет неизвестного типа: " + itemType + " - " + itemName);
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
            System.out.println("-------------------------");

            r.lines()
                    .skip(1)
                    .map(line -> {
                        String[] parts = line.split(",");
                        if (parts.length >= 3) {
                            try {
                                return new ScoreEntry(parts[1], Integer.parseInt(parts[2]), parts[0]);
                            } catch (NumberFormatException e) {
                                return null;
                            }
                        }
                        return null;
                    })
                    .filter(Objects::nonNull)
                    .sorted(Comparator.comparingInt(ScoreEntry::score).reversed())
                    .limit(10)
                    .forEach(entry -> System.out.printf("%s - %d очков (%s)%n",
                            entry.player(), entry.score(), entry.timestamp()));

        } catch (IOException e) {
            System.err.println("Ошибка чтения результатов: " + e.getMessage());
        }
    }

    private static void writeScore(String player, int score) {
        try (BufferedWriter w = Files.newBufferedWriter(SCORES,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {

            // Если файл пустой, добавляем заголовок
            if (Files.size(SCORES) == 0) {
                w.write("timestamp,player,score");
                w.newLine();
            }

            w.write(LocalDateTime.now() + "," + player + "," + score);
            w.newLine();

        } catch (IOException e) {
            System.err.println("Не удалось записать очки: " + e.getMessage());
        }
    }

    private record ScoreEntry(String player, int score, String timestamp) {
    }
}