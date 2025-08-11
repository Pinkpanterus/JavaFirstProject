package Homeworks.Homework06.Shop.FSM;

import Homeworks.Homework06.Shop.App;
import java.util.Objects;


public class FiniteStateMachine {
    private final App app;
    private State currentState = null;

    public FiniteStateMachine(App app) {
        this.app = app;
    }

    public void setState(State state) {
        if (this.currentState != null)
            this.currentState.exitState();

        if (state != null  && !Objects.equals(this.currentState, state)) {
            this.currentState = state;
            this.currentState.enterState();
        }
    }

//    public void execute(String input) {
//        if (this.currentState != null)
//            this.currentState.execute(input, app);
//    }

    public State getState() {
        return this.currentState;
    }
}
