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
        System.out.printf("Привет %s! %n", name);
    }

    private static void task2()
    {
        System.out.println("Задание 2");

        Random random = new Random();
        int vasyaChoice = random.nextInt(3);
        int petyaChoice = random.nextInt(3);
        final String[] NAMES = {"камень", "ножницы", "бумага"};

        System.out.printf("Выбор Васи: %s %n", NAMES[vasyaChoice]);
        System.out.printf("Выбор Пети: %s %n", NAMES[petyaChoice]);

        int result = whoWins(vasyaChoice, petyaChoice);
        switch (result)
        {
            case 0  -> System.out.println("Результат: ничья!");
            case 1  -> System.out.println("Результат: победил Вася!");
            default -> System.out.println("Результат: победил Петя!");
        }
    }

    private static int whoWins(int a, int b)
    {
        if (a == b) return 0;                    // ничья
        return ((a + 1) % 3 == b) ? 1 : 2;       // иначе побеждает a или b
    }

    public static void main(String[] args)
    {
        task1();
        task2();
    }
}
