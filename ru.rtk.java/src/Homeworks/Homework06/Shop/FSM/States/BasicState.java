package Homeworks.Homework06.Shop.FSM.States;

import Homeworks.Homework06.Shop.App;
import Homeworks.Homework06.Shop.FSM.State;

public abstract class BasicState implements State {
    @Override
    public void enterState() {
//        System.out.printf("Current state: %s%n", getStateName());
    }

    @Override
    public void execute(String input, App app) {

    }

    @Override
    public void exitState() {

    }

    @Override
    public String getStateName() {
        return getClass().getName();
    }
}
