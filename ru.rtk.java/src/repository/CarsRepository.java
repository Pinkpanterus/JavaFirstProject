package repository;

import model.Car;
import java.util.List;
import java.util.Optional;

public interface CarsRepository {
    List<Car> findAll();
    List<String> findNumbersByColorOrMileage(String color, long mileage);
    long countUniqueModelsInCostRange(long min, long max);
    Optional<String> findColorOfCheapestCar();
    double averageCostByModel(String model);
}