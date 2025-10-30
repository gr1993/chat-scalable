package org.loadtester.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class FileLogger {
    private static BufferedWriter writer;

    public static void init(String filePath) throws IOException {
        writer = new BufferedWriter(new FileWriter(filePath, true));  // append mode
    }

    public static synchronized void log(String message) {
        try {
            writer.write(message);
            writer.newLine();
            writer.flush(); // 즉시 저장
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void close() {
        try {
            if (writer != null) writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
