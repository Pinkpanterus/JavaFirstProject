package Homework_4;

import java.util.Scanner;

public class HW_4_1 {
    public static void main(String[] args){
        final String keyboard = "qwertyuiopasdfghjklzxcvbnm";
        System.out.println("Введите одну букву латинского алфавита");
        Scanner scanner = new Scanner(System.in);
        String string = scanner.nextLine();
        scanner.close();

        int index = keyboard.indexOf(string.toLowerCase().charAt(0));
        int prevIndex = (index - 1 + keyboard.length()) % keyboard.length();
        char ch = keyboard.charAt(prevIndex);
        System.out.printf("Буква слева от заданной буквы %s! %n", ch);
    }

}
