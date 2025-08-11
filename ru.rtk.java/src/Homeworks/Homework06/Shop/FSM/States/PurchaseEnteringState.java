package Homeworks.Homework06.Shop.FSM.States;

import Homeworks.Homework06.Shop.App;
import Homeworks.Homework06.Shop.Person;
import Homeworks.Homework06.Shop.Product;

public class PurchaseEnteringState extends BasicState {


    @Override
    public void enterState() {
        System.out.println("В каждой новой строке введите покупку в формате (ФИО - Товар): Павел Андреевич - Торт");
    }

    @Override
    public void execute(String input, App app) {
        String[] parts = input.split("-");
        if (parts.length != 2) return;
        String personName = parts[0].trim();
        String productName = parts[1].trim();

        Person person = app.getPersons().get(personName);
        Product product = app.getProducts().get(productName);

        if (person != null && product != null) {
            person.buyProduct(product);
        } else {
            System.out.println(person == null ? "Person not found" : "Product not found");
        }
    }
}
