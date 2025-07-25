package Attestation.Attestation01;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

public class App {
    public static void main(String[] args){
        Scanner scanner = new Scanner(System.in);

        Map<String, Person> persons = new LinkedHashMap<>();
        String[] personsData = scanner.nextLine().split(";");
        for (String personData : personsData) {
            String[] kv = personData.split("=");
            String name = kv[0].trim();
            int money;
            try {
                money = Integer.parseInt(kv[1].trim());
            } catch (NumberFormatException e) {
                System.out.println("Деньги не могут быть отрицательными");
                continue;
            }
            try {
                persons.put(name, new Person(name, money));
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        }

        Map<String, Product> products = new LinkedHashMap<>();
        String[] productsData = scanner.nextLine().split(";");
        for (String token : productsData) {
            String[] kv = token.split("=");
            String name = kv[0].trim();
            int cost;
            try {
                cost = Integer.parseInt(kv[1].trim());
            } catch (NumberFormatException e) {
                System.out.println("Стоимость не может быть отрицательной");
                continue;
            }
            try {
                products.put(name, new Product(name, cost));
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        }

        while (true) {
            String line = scanner.nextLine().trim();
            if ("END".equalsIgnoreCase(line)) break;

            String[] parts = line.split("-");
            if (parts.length != 2) continue;
            String personName = parts[0].trim();
            String productName = parts[1].trim();

            Person person = persons.get(personName);
            Product product = products.get(productName);

            if (person != null && product != null) {
                person.buyProduct(product);
            }
            else System.out.println(person == null ? "Person not found" : "Product not found");
        }

        persons.values().forEach(System.out::println);
        scanner.close();
    }
}
