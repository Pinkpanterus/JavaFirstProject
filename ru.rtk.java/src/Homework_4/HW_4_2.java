package Homework_4;

import java.util.Scanner;

public class HW_4_2 {
    public static void main(String[] args) {
        final String[] arrows = {">>-->", "<--<<"};
        System.out.println("Строку со стрелами >>--> и <--<<");
        Scanner scanner = new Scanner(System.in);
        String enteredString = scanner.nextLine();

        int cnt = 0;
        for (String arrow : arrows) {
            int pos = 0;
            while ((pos = enteredString.indexOf(arrow, pos)) != -1) {
                cnt++;
                pos += 1;
            }
        }
        System.out.printf("Количество стрел в строке: %s! %n", cnt);
    }
}
