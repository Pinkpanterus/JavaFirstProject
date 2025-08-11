package Homeworks.Homework06.Shop;

import Homeworks.Homework06.Shop.FSM.FiniteStateMachine;
import Homeworks.Homework06.Shop.FSM.State;
import Homeworks.Homework06.Shop.FSM.States.*;

import java.util.*;

public class App {
    private static Map<String, Person> persons = new LinkedHashMap<>();
    private static Map<String, Product> products = new LinkedHashMap<>();
    private static final Scanner scanner = new Scanner(System.in);
    private static boolean isRunning = true;
    private static FiniteStateMachine fsm;
    private static App app;

    public static void main(String[] args) {
        app = new App();
        fsm = new FiniteStateMachine(app);
        fsm.setState(new EnterState());

        String input = "";
        while (isRunning) {
            if (scanner.hasNextLine()) input = scanner.nextLine().trim();
            checkState(input);
            executeState(input);
        }
    }

    private static void checkState(String input) {
        State fsmCurrentState = fsm.getState();
        State newFsmState = null;

        if (fsmCurrentState instanceof EnterState)
            newFsmState = new PersonEnteringState();

        if (fsmCurrentState instanceof ProductEnteringState)
            newFsmState = new PurchaseEnteringState();

        if (input.contains("END"))
            newFsmState = new EndState();
        else if (input.contains("=")) {
            if (fsmCurrentState instanceof PersonEnteringState)
                newFsmState = new ProductEnteringState();
        }

        if (newFsmState != null && fsmCurrentState.getClass() != newFsmState.getClass()) {
            fsm.setState(newFsmState);
        }
    }

    private static void executeState(String input) {
        State fsmCurrentState = fsm.getState();
        if (Objects.equals(input, "") && !(fsmCurrentState instanceof EndState)) return;

        if (fsmCurrentState != null)
            fsmCurrentState.execute(input, app);
    }

    public static Map<String, Person> getPersons() {
        return persons;
    }

    public static Map<String, Product> getProducts() {
        return products;
    }

    public void addProduct(String name, int cost) {
        products.put(name, new Product(name, cost));
    }

    public void addPerson(String name, int money) {
        persons.put(name, new Person(name, money));
    }

    public void finish() {
        isRunning = false;
        scanner.close();

        if (!persons.isEmpty()) {
            persons.values().forEach(System.out::println);
        } else {
            System.out.println("Список покупателей пуст");
        }
    }
}