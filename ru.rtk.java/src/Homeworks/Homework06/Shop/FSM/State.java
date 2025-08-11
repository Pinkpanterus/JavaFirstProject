package Homeworks.Homework06.Shop.FSM;

import Homeworks.Homework06.Shop.App;

public interface State {
//    FiniteStateMachine fsm = null;
    void enterState();
    void execute(String input, App app);
    void exitState();
    String getStateName();
}
