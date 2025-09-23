package com.example.dungeon.core;

import com.example.dungeon.model.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.stream.Collectors;

public class Game {
    private final GameState state = new GameState();
    private final Map<String, Command> commands = new LinkedHashMap<>();
    private final Map<String, Room> rooms = new HashMap<>();

    static {
        WorldInfo.touch("Game");
    }

    public Game() {
        registerCommands();
        bootstrapWorld();
    }

    private void registerCommands() {
        commands.put("help", (ctx, a) -> {
            System.out.println("=== КОМАНДЫ ===");
            System.out.println("move <направление> - перемещение между комнатами");
            System.out.println("open <направление> - попытаться открыть дверь");
            System.out.println("examine <направление> - осмотреть выход");
            System.out.println("look - осмотреть текущую комнату");
            System.out.println("take <предмет> - взять предмет");
            System.out.println("use <предмет> - использовать предмет");
            System.out.println("inventory - показать инвентарь");
            System.out.println("fight - сразиться с монстром");
            System.out.println("wait - отдохнуть и восстановить HP");
            System.out.println("assess - оценить угрозу в текущей комнате");
            System.out.println("save - сохранить игру");
            System.out.println("load - загрузить игру");
            System.out.println("scores - таблица лидеров");
            System.out.println("gc-stats - статистика памяти");
            System.out.println("about - информация о игре");
            System.out.println("exit - выход из игры");
        });

        commands.put("about", (ctx, a) -> {
            System.out.println("DungeonMini v1.0");
            System.out.println("Разработчик: Гроностайский А.И.");
            System.out.println("Java version: " + System.getProperty("java.version"));
        });

        commands.put("gc-stats", (ctx, a) -> {
            Runtime rt = Runtime.getRuntime();
            long free = rt.freeMemory(), total = rt.totalMemory(), used = total - free;
            long max = rt.maxMemory();

            System.out.println("Память JVM:");
            System.out.println("  Used: " + (used / 1024 / 1024) + " MB");
            System.out.println("  Free: " + (free / 1024 / 1024) + " MB");
            System.out.println("  Total: " + (total / 1024 / 1024) + " MB");
            System.out.println("  Max: " + (max / 1024 / 1024) + " MB");

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

        commands.put("look", (ctx, a) -> System.out.println(ctx.getCurrentRoom().getDescription()));

        commands.put("move", (ctx, a) -> {
            if (a.isEmpty()) {
                throw new InvalidCommandException("Укажите направление: north, south, east, west");
            }

            String direction = a.getFirst().toLowerCase();
            Room currentRoom = ctx.getCurrentRoom();
            Way way = currentRoom.getWay(direction);

            if (way == null) {
                throw new InvalidCommandException("Нет выхода в направлении: " + direction);
            }

            if (way.isBlocked()) {
                throw new InvalidCommandException("Проход заблокирован. Используйте 'open " + direction + "' чтобы открыть дверь");
            }

            Room nextRoom = way.getDestination();

            // Проверяем засаду при входе в комнату с монстром
            if (nextRoom.getMonster() != null && Math.random() < 0.4) {
                nextRoom.monsterAmbush(ctx.getPlayer());

                // Проверяем смерть игрока после засады
                if (ctx.getPlayer().getCurrentHP() <= 0) {
                    System.out.println("Вы погибли от засады! Игра окончена.");
                    System.exit(0);
                }
            }

            ctx.setCurrentRoom(nextRoom);
            System.out.println("Вы перешли в: " + nextRoom.getName());
            System.out.println(nextRoom.getDescription());

            // Проверяем обычную атаку монстра после перемещения
            if (nextRoom.checkMonsterAttack(ctx.getPlayer())) {
                if (ctx.getPlayer().getCurrentHP() <= 0) {
                    System.out.println("Вы погибли! Игра окончена.");
                    System.exit(0);
                }
            }
        });

        commands.put("open", (ctx, a) -> {
            if (a.isEmpty()) {
                throw new InvalidCommandException("Укажите направление двери");
            }

            String direction = a.getFirst().toLowerCase();
            Room currentRoom = ctx.getCurrentRoom();
            Player player = ctx.getPlayer();

            String result = currentRoom.tryOpenDoor(direction, player);
            System.out.println(result);

            if (result.startsWith("Дверь открыта")) {
                ctx.addScore(5);
            }
        });

        commands.put("examine", (ctx, a) -> {
            if (a.isEmpty()) {
                throw new InvalidCommandException("Укажите направление для осмотра: north, south, east, west");
            }

            String direction = a.getFirst().toLowerCase();
            Room currentRoom = ctx.getCurrentRoom();
            Way way = currentRoom.getWay(direction);

            if (way == null) {
                throw new InvalidCommandException("Нет выхода в направлении: " + direction);
            }

            System.out.println("Осмотр выхода " + direction + ":");
            System.out.println("  Следующая комната: " + way.getDestination().getName());

            if (way.getDoor() != null) {
                Door door = way.getDoor();
                System.out.println("  Дверь: " + door.getName());
                System.out.println("  Состояние: " + (door.isLocked() ? "Заперта" : "Открыта"));

                if (door.isLocked() && door.getOpenningKey() != null) {
                    System.out.println("  Требуется ключ: " + door.getOpenningKey().getName());

                    boolean hasKey = ctx.getPlayer().getInventory().stream()
                            .anyMatch(item -> item instanceof Key &&
                                    item.getName().equals(door.getOpenningKey().getName()));

                    System.out.println("  У вас " + (hasKey ? "есть этот ключ" : "нет этого ключа") + ".");

                    if (hasKey) {
                        System.out.println("  Используйте команду 'open " + direction + "' чтобы открыть дверь");
                    }
                }
            } else {
                System.out.println("  Свободный проход");
            }
        });

        commands.put("take", (ctx, a) -> {
            if (a.isEmpty()) {
                throw new InvalidCommandException("Укажите название предмета");
            }

            String itemName = String.join(" ", a);
            Room currentRoom = ctx.getCurrentRoom();
            Player player = ctx.getPlayer();

            Item foundItem = currentRoom.getItems().stream()
                    .filter(item -> item.getName().equalsIgnoreCase(itemName))
                    .findFirst()
                    .orElse(null);

            if (foundItem == null) {
                throw new InvalidCommandException("Предмет '" + itemName + "' не найден в комнате");
            }

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

            foundItem.apply(ctx);
        });

        commands.put("fight", (ctx, a) -> {
            Room currentRoom = ctx.getCurrentRoom();
            Player player = ctx.getPlayer();
            Monster monster = currentRoom.getMonster();

            if (monster == null) {
                throw new InvalidCommandException("В этой комнате нет монстров для боя");
            }

            System.out.println("Начинается бой с " + monster.getName() + "!");

            while (player.getCurrentHP() > 0 && monster.getCurrentHP() > 0) {
                // Игрок атакует
                int playerDamage = player.getAttackDamage();
                monster.setCurrentHP(monster.getCurrentHP() - playerDamage);
                System.out.println("Вы бьёте " + monster.getName() + " на " + playerDamage +
                        ". HP монстра: " + Math.max(0, monster.getCurrentHP()));

                if (monster.getCurrentHP() <= 0) {
                    System.out.println("Вы победили " + monster.getName() + "!");

                    Item lootItem = monster.getLootItem();
                    if (lootItem != null) {
                        currentRoom.getItems().add(lootItem);
                        System.out.println("Из " + monster.getName() + " выпало: " + lootItem.getName());
                    }

                    currentRoom.setMonster(null);
                    ctx.addScore(10);
                    return;
                }

                // Монстр атакует (более агрессивно в бою)
                int monsterDamage = monster.getAttackDamage() + (int)(Math.random() * 3); // Случайный урон
                player.setCurrentHP(player.getCurrentHP() - monsterDamage);
                System.out.println(monster.getName() + " отвечает на " + monsterDamage +
                        ". Ваше HP: " + Math.max(0, player.getCurrentHP()));

                if (player.getCurrentHP() <= 0) {
                    System.out.println("Вы погибли! Игра окончена.");
                    System.exit(0);
                }

                // Шанс дополнительной атаки монстра
                if (Math.random() < 0.2) { // 20% шанс двойной атаки
                    int extraDamage = monster.getAttackDamage();
                    player.setCurrentHP(player.getCurrentHP() - extraDamage);
                    System.out.println(monster.getName() + " проводит быструю дополнительную атаку! Урон: " + extraDamage);
                    System.out.println("Ваше HP: " + Math.max(0, player.getCurrentHP()));

                    if (player.getCurrentHP() <= 0) {
                        System.out.println("Вы погибли от комбо атаки! Игра окончена.");
                        System.exit(0);
                    }
                }

                // Пауза между раундами
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });

        commands.put("wait", (ctx, a) -> {
            Room currentRoom = ctx.getCurrentRoom();
            Player player = ctx.getPlayer();

            System.out.println("Вы решили передохнуть...");

            // Восстановление HP при отдыхе (если нет монстров)
            if (currentRoom.getMonster() == null) {
                if (player.getCurrentHP() == player.getMaxHP()){
                    System.out.println("У вас уже максимальное количество HP, отдых его не может изменить.");
                }
                else {
                    int heal = 1;
                    player.setCurrentHP(player.getCurrentHP() + heal);
                    System.out.println("Отдых восстановил " + heal + " HP. Теперь у вас " + player.getCurrentHP() + " HP.");
                }
            } else {
                // Если в комнате есть монстр, он может атаковать
                System.out.println("Но здесь небезопасно!");
                if (currentRoom.checkMonsterAttack(player)) {
                    if (player.getCurrentHP() <= 0) {
                        System.out.println("Вы погибли во время отдыха! Игра окончена.");
                        System.exit(0);
                    }
                }
            }

            ctx.addScore(1);
        });

        commands.put("assess", (ctx, a) -> {
            Room currentRoom = ctx.getCurrentRoom();
            Monster monster = currentRoom.getMonster();

            if (monster == null) {
                System.out.println("В комнате нет угроз. Можно расслабиться.");
                return;
            }

            System.out.println("Оценка угрозы: " + monster.getName());
            System.out.println("Уровень: " + monster.getLevel() + " | HP: " + monster.getCurrentHP());

            if (monster instanceof AggressiveMonster) {
                System.out.println("⚠️  Этот монстр очень агрессивен! Атакует часто и сильно.");
            } else if (monster instanceof TrickyMonster) {
                System.out.println("🦊 Хитрая тварь! Может уклоняться от атак.");
            } else {
                System.out.println("☠️  Стандартная угроза. Будьте осторожны.");
            }

            // Сравнение сил
            Player player = ctx.getPlayer();
            if (player.getAttackDamage() > monster.getLevel() * 2) {
                System.out.println("✅ Вы явно сильнее этого противника.");
            } else if (player.getAttackDamage() < monster.getLevel()) {
                System.out.println("❌ Противник опасен! Лучше подготовиться.");
            } else {
                System.out.println("⚖️  Силы примерно равны. Будет тяжелый бой.");
            }
        });
        commands.put("save", (ctx, a) -> SaveLoad.save(ctx, rooms));
        commands.put("load", (ctx, a) -> SaveLoad.load(ctx, this::findRoomByName));
        commands.put("scores", (ctx, a) -> SaveLoad.printScores());
        commands.put("exit", (ctx, a) -> {
            System.out.println("Пока!");
            System.exit(0);
        });
    }

private void bootstrapWorld() {
    Player hero = new Player("Сталкер", 100, 15);
    state.setPlayer(hero);

    // Создаем все комнаты в стиле Сталкер
    Room cordon = new Room("Кордон", "Заброшенный военный кордон. Вокруг разбитая техника и следы боев.");
    Room carPark = new Room("Автостоянка", "Заброшенная стоянка с ржавыми автомобилями. Пахнет бензином и озоном.");
    Room trainTunnel = new Room("Железнодорожный тоннель", "Темный тоннель с застрявшим поездом. Слышны странные звуки.");
    Room darkValley = new Room("Темная долина", "Глубокое ущелье с аномальной активностью. Воздух мерцает.");
    Room armyWarehouses = new Room("Армейские склады", "Заброшенные военные склады. Много ящиков с припасами.");
    Room agroprom = new Room("Агропром", "Разрушенный научный комплекс. Высокий радиационный фон.");
    Room bar = new Room("Бар '100 рентген'", "Пристанище сталкеров. Тепло и уютно, пахнет водкой.");
    Room rostok = new Room("Росток", "Эко-станция сталкеров. Чисто и безопасно.");
    Room garbage = new Room("Свалка", "Гора металлолома и отходов. Опасная зона.");
    Room darkHollow = new Room("Темная впадина", "Аномальная зона с гравитационными аномалиями.");
    Room factory = new Room("Завод 'Янтарь'", "Заброшенный завод по переработке. Полно мутантов.");
    Room radar = new Room("Радар", "Секретный военный объект. Мощное пси-излучение.");
    Room laboratory = new Room("Подземная лаборатория X-10", "Секретная лаборация ученых. Много артефактов.");
    Room brainScorcher = new Room("Выжигатель мозгов", "Установка пси-излучения. Очень опасно!");
    Room cnpp = new Room("ЧАЭС", "Чернобыльская АЭС. Эпицентр Зоны.");
    Room sarcophagus = new Room("Саркофаг", "Укрытие 4-го энергоблока. Высокая аномальная активность.");
    Room wishGranter = new Room("Исполнитель желаний", "Легендарный артефакт в глубинах Саркофага.");
    Room freedomBase = new Room("База 'Свобода'", "Лагерь группировки Свобода.");
    Room dutyBase = new Room("База 'Долг'", "Укрепленная база группировки Долг.");
    Room monolith = new Room("Монолит", "Таинственный кристалл в центре Зоны.");

    // Создаем ключи и специальные предметы
    Key militaryKey = new Key("Ключ от военного бункера");
    Key labKey = new Key("Ключ от лаборатории X-10");
    Key cnppKey = new Key("Ключ от ЧАЭС");
    Key monolithKey = new Key("Ключ Монолита");

    // Создаем уникальные артефакты
    Item nightStar = new Item("Ночная звезда") {
        @Override
        public void apply(GameState ctx) {
            Player p = ctx.getPlayer();
            p.setMaxHP(p.getMaxHP() + 30);
            p.setAttackDamage(p.getAttackDamage() + 5);
            System.out.println("Артефакт 'Ночная звезда' активирован! +30 HP, +5 атаки");
            p.getInventory().remove(this);
        }
    };

    Item soul = new Item("Душа") {
        @Override
        public void apply(GameState ctx) {
            Player p = ctx.getPlayer();
            p.setMaxHP(p.getMaxHP() + 50);
            System.out.println("Артефакт 'Душа' исцеляет вас! +50 HP");
            p.getInventory().remove(this);
        }
    };

    Item compass = new Item("Компас выживания") {
        @Override
        public void apply(GameState ctx) {
            System.out.println("Компас показывает безопасные пути...");
            ctx.addScore(10);
        }
    };

    Item gaussRifle = new Weapon("Гаусс-винтовка", 25);

    // Создаем двери
    Door bunkerDoor = new Door("Дверь бункера", true, militaryKey);
    Door labDoor = new Door("Бронедверь лаборатории", true, labKey);
    Door cnppDoor = new Door("Шлюз ЧАЭС", true, cnppKey);
    Door monolithDoor = new Door("Врата Монолита", true, monolithKey);

    // Строим карту Зоны (20 комнат с интересными связями)

    // Стартовая зона - Кордон
    cordon.addWay("north", carPark);
    cordon.addWay("east", trainTunnel);

    // Зона начальной сложности
    carPark.addWay("south", cordon);
    carPark.addWay("east", darkValley);
    carPark.addWay("north", garbage);

    trainTunnel.addWay("west", cordon);
    trainTunnel.addWay("east", bar);
    trainTunnel.addWayWithDoor("north", armyWarehouses, bunkerDoor);

    // Средняя зона сложности
    darkValley.addWay("west", carPark);
    darkValley.addWay("north", agroprom);

    armyWarehouses.addWay("south", trainTunnel);
    armyWarehouses.addWay("east", rostok);
    armyWarehouses.addWay("north", factory);

    bar.addWay("west", trainTunnel);
    bar.addWay("east", rostok);

    rostok.addWay("west", bar);
    rostok.addWay("south", armyWarehouses);
    rostok.addWay("north", darkHollow);

    garbage.addWay("south", carPark);
    garbage.addWay("east", agroprom);

    agroprom.addWay("south", darkValley);
    agroprom.addWay("west", garbage);
    agroprom.addWay("north", factory);

    // Опасная зона
    darkHollow.addWay("south", rostok);
    darkHollow.addWay("east", radar);

    factory.addWay("south", armyWarehouses);
    factory.addWay("west", agroprom);
    factory.addWay("east", radar);
    factory.addWayWithDoor("north", laboratory, labDoor);

    radar.addWay("west", darkHollow);
    radar.addWay("west", factory);
    radar.addWay("north", brainScorcher);

    // Конечная зона - высокая сложность
    laboratory.addWay("south", factory);
    laboratory.addWay("east", brainScorcher);
    laboratory.addWayWithDoor("north", cnpp, cnppDoor);

    brainScorcher.addWay("west", laboratory);
    brainScorcher.addWay("south", radar);
    brainScorcher.addWay("north", freedomBase);

    freedomBase.addWay("south", brainScorcher);
    freedomBase.addWay("east", dutyBase);

    dutyBase.addWay("west", freedomBase);
    dutyBase.addWay("north", cnpp);

    cnpp.addWay("south", laboratory);
    cnpp.addWay("south", dutyBase);
    cnpp.addWay("east", sarcophagus);

    sarcophagus.addWay("west", cnpp);
    sarcophagus.addWay("north", wishGranter);
    sarcophagus.addWayWithDoor("east", monolith, monolithDoor);

    wishGranter.addWay("south", sarcophagus);
    monolith.addWay("west", sarcophagus);

    // Расставляем предметы по комнатам
    cordon.getItems().add(new Potion("Аптечка", 20));
    carPark.getItems().add(new Weapon("Обрез", 8));
    trainTunnel.getItems().add(new Potion("Армейская аптечка", 40));
    darkValley.getItems().add(nightStar);
    armyWarehouses.getItems().add(new Potion("Элитная аптечка", 60));
    bar.getItems().add(new Potion("Водка с перцем", 10));
    rostok.getItems().add(compass);
    garbage.getItems().add(new Weapon("АКМ-74/2", 12));
    agroprom.getItems().add(militaryKey);
    darkHollow.getItems().add(soul);
    factory.getItems().add(labKey);
    radar.getItems().add(new Potion("Антирадин", 30));
    laboratory.getItems().add(gaussRifle);
    laboratory.getItems().add(cnppKey);
    brainScorcher.getItems().add(new Potion("Пси-блокатор", 25));
    freedomBase.getItems().add(new Weapon("Винторез", 18));
    dutyBase.getItems().add(new Potion("Стимулятор", 15));
    cnpp.getItems().add(monolithKey);
    sarcophagus.getItems().add(new Potion("Экстрим", 50));

    // Расставляем монстров с лутом
    // Мелкие мутанты в начальной зоне
    carPark.setMonster(new Monster("Тушкан", 2, 30, 5 ,new Potion("Мясо тушкана", 5)));
    trainTunnel.setMonster(new Monster("Слепой пёс", 3, 40, 10, new Item("Клык пса") {
        @Override public void apply(GameState ctx) { System.out.println("Клык на удачу! +3 очка"); ctx.addScore(3); }
    }));

    // Средние мутанты
    darkValley.setMonster(new Monster("Кровосос", 5, 60, 20, new Potion("Железа кровососа", 20)));
    garbage.setMonster(new Monster("Кабан-мутант", 6, 80, 25, new Weapon("Клык кабана", 5)));
    agroprom.setMonster(new Monster("Псевдогигант", 8, 100, 30, new Potion("Мускульная ткань", 40)));

    // Враждебные сталкеры
    armyWarehouses.setMonster(new Monster("Мародёр", 4, 50, 25, new Weapon("ПМ", 7)));
    factory.setMonster(new Monster("Наёмник", 7, 70, 30, new Potion("Стимпак", 25)));

    // Опасные мутанты и аномалии
    darkHollow.setMonster(new Monster("Хим-призрак", 10, 90, 35, new Item("Аномальный сгусток") {
        @Override public void apply(GameState ctx) {
            ctx.getPlayer().setMaxHP(ctx.getPlayer().getMaxHP() - 10);
            ctx.getPlayer().setAttackDamage(ctx.getPlayer().getAttackDamage() + 10);
            System.out.println("Аномалия усилила атаку, но повредила здоровье!");
        }
    }));

    radar.setMonster(new Monster("Пси-призрак", 12, 110, 25, new Potion("Пси-защита", 35)));
    laboratory.setMonster(new Monster("Контактёр", 15, 130, 30, new Item("Данные исследований") {
        @Override public void apply(GameState ctx) { System.out.println("Секретные данные! +20 очков"); ctx.addScore(20); }
    }));

    // Боссы
    brainScorcher.setMonster(new Monster("ПСИ-излучатель", 20, 200, 35, new Item("Ядро излучателя") {
        @Override public void apply(GameState ctx) {
            ctx.getPlayer().setAttackDamage(ctx.getPlayer().getAttackDamage() + 15);
            System.out.println("Мощь ПСИ-излучателя теперь ваша! +15 атаки");
        }
    }));

    cnpp.setMonster(new Monster("Монолитовец", 18, 180, 35, new Potion("Эликсир Зоны", 80)));
    sarcophagus.setMonster(new Monster("Стрелок", 25, 250, 40, new Weapon("Граната Ф-1", 30)));

    // Финальный босс
    monolith.setMonster(new Monster("Страж Монолита", 30, 300, 50, new Item("Сердце Зоны") {
        @Override public void apply(GameState ctx) {
            System.out.println("Вы достигли цели! Зона покорена! +100 очков");
            ctx.addScore(100);
            System.out.println("ПОБЕДА! Вы стали легендой Зоны!");
        }
    }));

    // Начинаем игру на Кордоне
    state.setCurrentRoom(cordon);

    // Сохраняем все комнаты
    rooms.put(cordon.getName(), cordon);
    rooms.put(carPark.getName(), carPark);
    rooms.put(trainTunnel.getName(), trainTunnel);
    rooms.put(darkValley.getName(), darkValley);
    rooms.put(armyWarehouses.getName(), armyWarehouses);
    rooms.put(agroprom.getName(), agroprom);
    rooms.put(bar.getName(), bar);
    rooms.put(rostok.getName(), rostok);
    rooms.put(garbage.getName(), garbage);
    rooms.put(darkHollow.getName(), darkHollow);
    rooms.put(factory.getName(), factory);
    rooms.put(radar.getName(), radar);
    rooms.put(laboratory.getName(), laboratory);
    rooms.put(brainScorcher.getName(), brainScorcher);
    rooms.put(freedomBase.getName(), freedomBase);
    rooms.put(dutyBase.getName(), dutyBase);
    rooms.put(cnpp.getName(), cnpp);
    rooms.put(sarcophagus.getName(), sarcophagus);
    rooms.put(wishGranter.getName(), wishGranter);
    rooms.put(monolith.getName(), monolith);
}

    public Room findRoomByName(String roomName) {
        return rooms.get(roomName);
    }

    public void run() {
        System.out.println("StalkerMini (v.1).");

        System.out.println("Вы просыпаетесь на Кордоне... Голова болит, в памяти пробелы.");
        System.out.println("Старый сталкер шепчет: 'Ищешь Исполнитель желаний? Пройди Зону и найди Монолит.'");
        System.out.println("Цель: добраться до Монолита в самом сердце ЧАЭС!");
        System.out.println("Используйте 'help' для списка команд, 'look' для осмотра местности.");

        try (BufferedReader in = new BufferedReader(new InputStreamReader(System.in))) {
            while (true) {
                System.out.print("> ");
                String line = in.readLine();
                if (line == null) break;
                line = line.trim();
                if (line.isEmpty()) continue;

                List<String> parts = Arrays.asList(line.split("\\s+"));
                String cmd = parts.getFirst().toLowerCase(Locale.ROOT);
                List<String> args = parts.subList(1, parts.size());

                Command c = commands.get(cmd);
                try {
                    if (c == null) throw new InvalidCommandException("Неизвестная команда: " + cmd);
                    c.execute(state, args);
                    state.addScore(1);
                } catch (InvalidCommandException e) {
                    System.out.println("Ошибка: " + e.getMessage());
                } catch (UncheckedIOException e) {
                    System.out.println("Ошибка ввода/вывода: " + e.getMessage());
                } catch (NumberFormatException e) {
                    System.out.println("Ошибка формата числа: " + e.getMessage());
                } catch (Exception e) {
                    System.out.println("Непредвиденная ошибка: " + e.getClass().getSimpleName() + ": " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.out.println("Критическая ошибка ввода/вывода: " + e.getMessage());
        }
    }
}