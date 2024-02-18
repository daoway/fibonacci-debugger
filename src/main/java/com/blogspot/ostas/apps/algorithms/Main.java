package com.blogspot.ostas.apps.algorithms;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;

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

            // Read output
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            // Read error
            BufferedReader errorReader =
                    new BufferedReader(new InputStreamReader(process.getErrorStream()));
            while ((line = errorReader.readLine()) != null) {
                System.err.println(line); // Print to stderr
            }

            // Wait for the process to finish
            int exitCode = process.waitFor();

            // Print the exit code
            System.out.println("JVM process exited with code: " + exitCode);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

    }
}
