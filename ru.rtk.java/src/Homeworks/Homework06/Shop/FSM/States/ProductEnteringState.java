package Homeworks.Homework06.Shop.FSM.States;

import Homeworks.Homework06.Shop.App;
import Homeworks.Homework06.Shop.FSM.State;

public class ProductEnteringState extends BasicState {
    @Override
    public void enterState() {
//        System.out.println("Введите товары в формате (Название = цена): Хлеб = 40; Молоко = 60");
        System.out.println("В каждой новой строке введите покупку в формате (ФИО - Товар): Павел Андреевич - Торт");
    }

    @Override
    public void execute(String input, App app) {
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
                app.addProduct(name, cost);
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
