package Homework_4;

import java.util.Arrays;
import java.util.Scanner;

public class HW_4_3 {
    public static void main(String[] args){
        System.out.println("Введите строку из двух слов на латинице:");
        Scanner scanner = new Scanner(System.in);
        String enteredString = scanner.nextLine();
        scanner.close();

        String[] splitedStrings = enteredString.split(" ");
        StringBuilder sb = new StringBuilder(enteredString.length());
        for (String string: splitedStrings){
            char[] chars = string.toLowerCase().toCharArray();
            Arrays.sort(chars);
            sb.append(chars);

            if (sb.length() < enteredString.length()) {
                sb.append(' ');
            }
        }
        System.out.printf("Отсортированные символы: %s %n", sb);
    }
}
