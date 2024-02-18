package com.blogspot.ostas.apps.algorithms;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        String classpath;
        try {
            classpath = Files.readString(Path.of("target", "classpath.txt"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        classpath += File.pathSeparator + "target" + File.separator + "classes";

        try {
            Process process = startJVMProcess(classpath);

            // Start threads to handle output and error streams
            startOutputReaderThread(process);
            startErrorReaderThread(process);

            // Read input from stdin and write to the process
            readInputAndWriteToProcess(process);

            // Wait for the process to finish
            int exitCode = process.waitFor();

            // Print the exit code
            System.out.println("JVM process exited with code: " + exitCode);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static Process startJVMProcess(String classpath) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder(
                "java",
                "-classpath",
                classpath,
                "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=8000", // Debug parameters
                FibonacciApp.class.getName()
        );
        return processBuilder.start();
    }

    private static void startOutputReaderThread(Process process) {
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
    }

    private static void startErrorReaderThread(Process process) {
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
    }

    private static void readInputAndWriteToProcess(Process process) {
        try (Scanner scanner = new Scanner(System.in);
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()))) {
            System.out.print("Enter a line: ");
            String inputLine = scanner.nextLine();
            writer.write(inputLine);
            writer.newLine();
            writer.flush(); // Flush the stream to ensure data is sent
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
