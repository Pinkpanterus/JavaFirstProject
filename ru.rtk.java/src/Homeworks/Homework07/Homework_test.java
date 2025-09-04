package Homeworks.Homework07;

import java.util.*;

import static Homeworks.Homework07.Task_1.getUniqueElements;
import static Homeworks.Homework07.Task_2.isAnagram;

public class Homework_test {

    public static void main(String[] args) {
        // Задание 1
        System.out.println("Задание 1");
        ArrayList<Integer> numbers = new ArrayList<>();
        Collections.addAll(numbers, 1, 2, 3, 3, 3, 4, 4, 4, 5);
        Set<Integer> uniqueElements = getUniqueElements(numbers);
        System.out.println("Уникальные элементы: " + uniqueElements);

        // Задание 2
        System.out.println("Задание 2");
        Scanner scanner = new Scanner(System.in);
        System.out.println("Введите первую строку: ");
        String s = scanner.nextLine();
        System.out.println("Введите вторую строку: ");
        String t = scanner.nextLine();

        boolean result = isAnagram(s, t);
        System.out.println("Являются ли строки анаграммами: " + result);
        scanner.close();

        // Задание 3
        System.out.println("Задание 3");
        Set<Integer> set1 = new HashSet<>();
        Collections.addAll(set1, 1, 2, 3);

        Set<Integer> set2 = new HashSet<>();
        Collections.addAll(set2, 0, 1, 2, 4);

        PowerfulSet powerfulSet = new PowerfulSet();

        System.out.println("Пересечение: " + powerfulSet. intersection(set1, set2));
        System.out.println("Объединение: " + powerfulSet.union(set1, set2));
        System.out.println("Разность: " + powerfulSet.relativeComplement(set1, set2));
    }
}
