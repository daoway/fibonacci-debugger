package com.blogspot.ostas.apps.algorithms;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        String cp;
        try {
            cp = Files.readString(Path.of("target", "classpath.txt"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        cp += File.pathSeparator + "target" + File.separator + "classes";

        try {
            // Command to launch the JVM with debug parameters
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "java",
                    "-classpath",
                    cp,
                    "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=8000", // Debug parameters
                    FibonacciApp.class.getName()
            );

            // Start the process
            Process process = processBuilder.start();

            // Thread to read output
            Thread outputReaderThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            outputReaderThread.start();

            // Thread to read error
            Thread errorReaderThread = new Thread(() -> {
                try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                    String line;
                    while ((line = errorReader.readLine()) != null) {
                        System.err.println(line); // Print to stderr
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            errorReaderThread.start();

            // Read input from stdin
            try (Scanner scanner = new Scanner(System.in)) {
                //System.out.print("Enter a line: ");
                String inputLine = scanner.nextLine();

                // Write to stdin of the spawned process
                try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()))) {
                    writer.write(inputLine);
                    writer.newLine();
                    writer.flush(); // Flush the stream to ensure data is sent
                }

            } // Scanner and its underlying stream will be closed automatically after this block

            // Wait for the process to finish
            int exitCode = process.waitFor();

            // Print the exit code
            System.out.println("JVM process exited with code: " + exitCode);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

    }
}
