package lab0;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class Helper {
    public static void randGenFile(String file, int size, int min, int max) {
        Random random = new Random();
        try (FileWriter fileWriter = new FileWriter(file)) {
            for (int i = 0; i < size; i++) {
                int number = min + random.nextInt(max - min);
                fileWriter.write(String.valueOf(number));
                if (i != size - 1) {
                    fileWriter.write(",");
                }
            }

            fileWriter.write(System.lineSeparator());
            fileWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean areFilesEqual(String file1, String file2) {
        try {
            byte[] file1Content = Files.readAllBytes(Path.of(file1));
            byte[] file2Content = Files.readAllBytes(Path.of(file2));
            if (file1Content.length != file2Content.length) {
                return false;
            }

            for (int i = 0; i < file1Content.length; i++) {
                if (file1Content[i] != file2Content[i]) {
                    return false;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public static void excelWriteLine(String file, String data, Duration duration) {
        try (FileWriter fileWriter = new FileWriter(file, true)) {
            fileWriter.write("Milliseconds:" + duration.toMillis() + "," + data + System.lineSeparator());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void excelWriteLine(String file, String data, Duration duration, int line) {
        List<String> lines = new ArrayList<>();
        try {
            lines = Files.readAllLines(Path.of(file));
        } catch (IOException e) {
            e.printStackTrace();
        }

        lines.add(line, "Milliseconds:" + duration.toMillis() + "," + data);
        try (FileWriter fileWriter = new FileWriter(file)) {
            for (String _line : lines) {
                fileWriter.write(_line + System.lineSeparator());
            }

            fileWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<Map.Entry<Duration, Integer>> triggerProgram(String command, String args, int numberOfRuns) {
        List<Map.Entry<Duration, Integer>> results = new ArrayList<>();
        ProcessBuilder builder = new ProcessBuilder(command, args);
        try {
            for (int i = 0; i < numberOfRuns; i++) {
                Instant start = Instant.now();
                Process process = builder.start();
                process.wait();
                Instant finish = Instant.now();
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                int statusCode = reader.read();
                results.add(new AbstractMap.SimpleEntry<>(Duration.between(start, finish), statusCode));
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            results.add(new AbstractMap.SimpleEntry<>(null, 1));
        }

        return results;
    }

    public static List<Map.Entry<Duration, Integer>> triggerProgram(Runnable runnable, int numberOfRuns) {
        List<Map.Entry<Duration, Integer>> results = new ArrayList<>();
        try {
            for (int i = 0; i < numberOfRuns; i++) {
                Instant start = Instant.now();
                runnable.run();
                runnable.wait();
                Instant finish = Instant.now();
                results.add(new AbstractMap.SimpleEntry<>(Duration.between(start, finish), 0));
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            results.add(new AbstractMap.SimpleEntry<>(null, 1));
        }

        return results;
    }
}
