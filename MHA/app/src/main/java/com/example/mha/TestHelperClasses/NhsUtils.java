package com.example.mha.TestHelperClasses;

import java.util.ArrayList;

public class NhsUtils {

    public static ArrayList<Integer> getDigits(String number) {
        ArrayList<Integer> result = new ArrayList<>();
        for (int i = 0; i < number.length(); i++) {
            result.add(number.charAt(i) - '0');
        }
        return result;
    }

    public static boolean verifyNhsNum(String nhsNumber) {
        if (nhsNumber.length() != 10) return false;

        ArrayList<Integer> digits = getDigits(nhsNumber);

        int checkDigit = digits.remove(9);

        int total = 0;
        int weight = 10;

        for (int i = 0; i < digits.size(); i++) {
            total += digits.get(i) * weight;
            weight--;
        }

        int remainder = total % 11;
        int expectedCheck = 11 - remainder;

        if (expectedCheck == 11) expectedCheck = 0;
        if (expectedCheck == 10) return false;

        return expectedCheck == checkDigit;
    }
}
