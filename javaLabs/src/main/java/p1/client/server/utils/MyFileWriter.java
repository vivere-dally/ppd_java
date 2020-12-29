package p1.client.server.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

public class MyFileWriter {
    private final String filePath;

    public MyFileWriter(String filePath) {
        String dir = Paths.get(".").toAbsolutePath().normalize().toString();
        this.filePath = Paths.get(dir, filePath).toString();
        File file = new File(filePath);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void writeAppend(String s) {
        try (FileWriter writer = new FileWriter(this.filePath, true)) {
            writer.write(s + System.lineSeparator());
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeAppend(Iterable<String> lines) {
        try (FileWriter writer = new FileWriter(this.filePath, true)) {
            for (String line : lines) {
                writer.write(line + System.lineSeparator());
            }

            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
