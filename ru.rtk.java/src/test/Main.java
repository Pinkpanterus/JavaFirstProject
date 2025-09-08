package test;

import model.Car;
import repository.CarsRepository;
import repository.CarsRepositoryImpl;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class Main {

    private static final String INPUT_FILE = "ru.rtk.java/src/data/cars.txt";
    private static final String OUTPUT_FILE = "out.txt";

    public static void main(String[] args) {
//        System.out.println("Текущая папка: " + Path.of("").toAbsolutePath());

        CarsRepository repo = new CarsRepositoryImpl(INPUT_FILE);

        // 1. Печать исходного списка
        List<Car> all = repo.findAll();
        StringBuilder sb = new StringBuilder();
        sb.append("Автомобили в базе:\n");
        sb.append("  Number     Model   Color Mileage       Cost\n");
        all.forEach(c -> sb.append("  ").append(c).append("\n"));

        // 2. Номера по цвету или нулевому пробегу
        String colorToFind = "Black";
        long mileageToFind = 0L;
        List<String> numbers = repo.findNumbersByColorOrMileage(colorToFind, mileageToFind);
        sb.append("Номера автомобилей по цвету или нулевому пробегу: ")
                .append(String.join(" ", numbers)).append("\n");

        // 3. Кол-во уникальных моделей в диапазоне цен
        long min = 700_000L, max = 800_000L;
        long unique = repo.countUniqueModelsInCostRange(min, max);
        sb.append("Уникальные автомобили: ").append(unique).append(" шт.\n");

        // 4. Цвет самой дешёвой машины
        String cheapestColor = repo.findColorOfCheapestCar().orElse("—");
        sb.append("Цвет автомобиля с минимальной стоимостью: ")
                .append(cheapestColor).append("\n");

        // 5. Средняя цена для каждой из моделей
        for (String m : List.of("Toyota", "Volvo")) {
            double avg = repo.averageCostByModel(m);
            sb.append("Средняя стоимость модели ")
                    .append(m).append(": ")
                    .append(String.format("%,.2f", avg))
                    .append("\n");
        }

        // Вывод в консоль
        System.out.print(sb);

        // Вывод в файл
        try (BufferedWriter w = Files.newBufferedWriter(
                Path.of(OUTPUT_FILE), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            w.write(sb.toString());
        } catch (IOException e) {
            System.err.println("Не удалось записать файл: " + e.getMessage());
        }
    }
}