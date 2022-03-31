package client;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class History {
    private static final int MAX_SIZE_HISTORY = 100;

    public static ArrayList<String> readLastHundredEntries (String path) throws IOException {
        ArrayList<String> historyList =  new ArrayList<>(Files.readAllLines(Paths.get(path)));
        ArrayList<String> lastHundredEntries = new ArrayList<>(100);

        int beginIndex;

        if (historyList.size() >= MAX_SIZE_HISTORY) {
            beginIndex = historyList.size() - MAX_SIZE_HISTORY;
        } else {
            beginIndex = 0;
        }

        for (int i = beginIndex; i < historyList.size(); i++) {
            lastHundredEntries.add(historyList.get(i));
        }

            return lastHundredEntries;
    }

    public static void writeHistory (String path, String msg) throws IOException {
        FileWriter outHistory = new FileWriter(path, true);
        outHistory.write(msg + "\r\n");
        outHistory.close();
    }
}
