package Homeworks.Homework06.Shop.FSM.States;

import Homeworks.Homework06.Shop.App;

public class EndState extends BasicState {
    @Override
    public void enterState() {
        System.out.println("Программа завершена");
    }

    @Override
    public void execute(String input, App app) {
        app.finish();
    }
}
