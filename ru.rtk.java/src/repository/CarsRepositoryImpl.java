package repository;

import model.Car;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class CarsRepositoryImpl implements CarsRepository {

    private final List<Car> cars;

    public CarsRepositoryImpl(String fileName) {
        cars = loadFromFile(fileName);
    }

    private List<Car> loadFromFile(String fileName) {
        try {
            return Files.lines(Path.of(fileName))
                    .filter(line -> !line.trim().isEmpty())
                    .map(this::parseLine)
                    .toList();
        } catch (IOException e) {
            System.err.println("Ошибка чтения файла: " + e.getMessage());
            return List.of();
        }
    }

    private Car parseLine(String line) {
        String[] p = line.split("\\|");
        return new Car(
                p[0].trim(),
                p[1].trim(),
                p[2].trim(),
                Long.parseLong(p[3].trim()),
                Long.parseLong(p[4].trim())
        );
    }

    @Override
    public List<Car> findAll() { return cars; }

    @Override
    public List<String> findNumbersByColorOrMileage(String color, long mileage) {
        return cars.stream()
                .filter(c -> c.getColor().equalsIgnoreCase(color) || c.getMileage() == mileage)
                .map(Car::getNumber)
                .toList();
    }

    @Override
    public long countUniqueModelsInCostRange(long minPrice, long maxPrice) {
        return cars.stream()
                .filter(c -> c.getCost() >= minPrice && c.getCost() <= maxPrice)
                .map(Car::getModel)
                .distinct()
                .count();
    }

    @Override
    public Optional<String> findColorOfCheapestCar() {
        return cars.stream()
                .min(Comparator.comparingLong(Car::getCost))
                .map(Car::getColor);
    }

    @Override
    public double averageCostByModel(String model) {
        return cars.stream()
                .filter(c -> c.getModel().equalsIgnoreCase(model))
                .mapToLong(Car::getCost)
                .average()
                .orElse(0.0);
    }
}