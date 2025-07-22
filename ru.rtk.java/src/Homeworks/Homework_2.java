package Homeworks;

import java.util.Random;
import java.util.Scanner;

public class Homework_2
{
    private static void task1()
    {
        System.out.println("Задание 1");
        Scanner scanner = new Scanner(System.in);
        System.out.println("Как тебя зовут?");
        String name = scanner.nextLine();
        scanner.close();
        System.out.printf("Привет %s! %n", name);
    }

    private static void task2()
    {
        System.out.println("Задание 2");

        final String[] NAMES = {"камень", "ножницы", "бумага"};
        final String[] RESULTS = {"ничья", "победил Вася", "победил Петя"};

        Random random = new Random();
        int vasyaChoice = random.nextInt(3);
        int petyaChoice = random.nextInt(3);

        System.out.printf("Выбор Васи: %s %n", NAMES[vasyaChoice]);
        System.out.printf("Выбор Пети: %s %n", NAMES[petyaChoice]);

        int winner = getWinner(vasyaChoice, petyaChoice);
        System.out.printf("Результат: %s! %n", RESULTS[winner]);
    }

    private static int getWinner(int choice1, int choice2)
    {
        if (choice1 == choice2) return 0;                    // ничья
        return ((choice1 + 1) % 3 == choice2) ? 1 : 2;       // иначе побеждает a или b
    }

    public static void main(String[] args)
    {
        task1();
        task2();
    }
}
