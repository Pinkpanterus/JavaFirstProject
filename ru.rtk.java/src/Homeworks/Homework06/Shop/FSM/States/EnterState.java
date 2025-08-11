package Homeworks.Homework06.Shop.FSM.States;

public class EnterState extends BasicState {
    @Override
    public void enterState() {
        System.out.println("Программа запущена. Для завершения введите - END");
        System.out.println("Введите покупателей в формате (ФИО = количество денег): Павел Андреевич = 10000; Анна Петровна = 2000");
    }
}
