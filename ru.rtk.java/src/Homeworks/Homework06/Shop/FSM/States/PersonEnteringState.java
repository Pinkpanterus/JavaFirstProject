package Homeworks.Homework06.Shop.FSM.States;

import Homeworks.Homework06.Shop.App;
import Homeworks.Homework06.Shop.FSM.State;

public class PersonEnteringState extends BasicState {
    @Override
    public void enterState() {
        System.out.println("Введите товары в формате (Название = цена): Хлеб = 40; Молоко = 60");
    }

    @Override
    public void execute(String input, App app) {
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
                app.addPerson(name, money);
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        }
    }
}