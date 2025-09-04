package Homeworks.Homework07;

public class Task_2 {
    public static boolean isAnagram(String s, String t) {
        if (s.length() != t.length()) {
            return false;
        }

        char[] sArray = s.toLowerCase().toCharArray();
        char[] tArray = t.toLowerCase().toCharArray();

        java.util.Arrays.sort(sArray);
        java.util.Arrays.sort(tArray);

        return java.util.Arrays.equals(sArray, tArray);
    }

}
