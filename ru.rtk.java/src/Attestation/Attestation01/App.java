package Attestation.Attestation01;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

public class App {
    private static State currentState = State.INITIAL_STATE;
    private static Map<String, Person> persons = new LinkedHashMap<>();
    private static Map<String, Product> products = new LinkedHashMap<>();

    public static void main(String[] args) {
        //Run cycle
        Scanner scanner = new Scanner(System.in);
        while (currentState != State.END_STATE) {
            String input = scanner.nextLine().trim();
            checkState(input);
            executeString(input, scanner);
        }
    }

    private static void endWork(Scanner scanner) {
        persons.values().forEach(System.out::println);
        scanner.close();
    }

    private static void addPurchase(String input) {
        String[] parts = input.split("-");
        if (parts.length != 2) return;
        String personName = parts[0].trim();
        String productName = parts[1].trim();

        Person person = persons.get(personName);
        Product product = products.get(productName);

        if (person != null && product != null) {
            person.buyProduct(product);
        } else {
            System.out.println(person == null ? "Person not found" : "Product not found");
        }
    }

    private static void addProducts(String input) {
        String[] productsData = input.split(";");
        for (String token : productsData) {
            String[] kv = token.split("=");
            if (kv.length != 2) continue;
            String name = kv[0].trim();
            int cost;
            try {
                cost = Integer.parseInt(kv[1].trim());
            } catch (NumberFormatException e) {
                System.out.println("Стоимость должна быть положительным числом");
                continue;
            }
            try {
                products.put(name, new Product(name, cost));
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private static void addPersons(String input) {
        String[] personsData = input.split(";");
        for (String personData : personsData) {
            String[] kv = personData.split("=");
            if (kv.length != 2) continue;
            String name = kv[0].trim();
            int money;
            try {
                money = Integer.parseInt(kv[1].trim());
            } catch (NumberFormatException e) {
                System.out.println("Деньги должны быть положительным числом");
                continue;
            }
            try {
                persons.put(name, new Person(name, money));
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private static void executeString(String input, Scanner scanner) {
//        System.out.printf("Текущее состояние: %s.%n", currentState);
        switch (currentState) {
            case PERSONS_ENTERING_STATE -> addPersons(input);
            case PRODUCTS_ENTERING_STATE -> addProducts(input);
            case PUCHASES_ENTERING_STATE -> addPurchase(input);
            case END_STATE -> endWork(scanner);
        }
    }

    private static void checkState(String input) {
//        System.out.printf("Текущее состояние: %s.%n", currentState);
        if (input.contains("END") && currentState != State.END_STATE) {
            currentState = State.END_STATE;
        } else if (input.contains("=")) {
            currentState = (currentState == State.INITIAL_STATE)
                    ? State.PERSONS_ENTERING_STATE
                    : State.PRODUCTS_ENTERING_STATE;
        }
        else if (input.contains("-") && currentState != State.PUCHASES_ENTERING_STATE)
            currentState = State.PUCHASES_ENTERING_STATE;
    }
}