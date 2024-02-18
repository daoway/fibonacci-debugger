package com.blogspot.ostas.apps.algorithms;

import java.util.Scanner;

public class FibonacciApp {
    public static void main(String[] args) {
        String line = readLineFromUser();
        System.out.println("You entered: " + line);

        int n = 50;
        int x0 = fib0(n); //1,1,2,3,5
        System.out.println(x0);
    }

    private static String readLineFromUser() {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.print("Enter a line: ");
            return scanner.nextLine();
        }
    }

    public static int fib0(int n) {
        if (n == 0) return 0;
        if (n == 1) return 1;
        return fib0(n - 1) + fib0(n - 2);
    }
}
