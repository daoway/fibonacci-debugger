package com.blogspot.ostas.apps.algorithms;

import java.util.Scanner;

// for use in debugger JVM param
// -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=8000
public class FibonacciApp {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter a line: ");
        var line = scanner.nextLine();
        System.out.println("You entered: " + line);

        scanner.close(); // Don't forget to close the scanner

        int n = 50;
        int x0 = fib0(n); //1,1,2,3,5
        System.out.println(x0);
    }
    public static int fib0(int n) {
        if (n == 0) return 0;
        if (n == 1) return 1;
        var result = fib0(n - 1) + fib0(n - 2);
        return result;
    }

}
