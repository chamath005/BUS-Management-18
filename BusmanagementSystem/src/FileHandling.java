import java.io.*;
import java.util.*;

public class FileHandling {

    public static final String DATA_FOLDER = "data";

    public static List<String[]> read(String fileName) {
        List<String[]> records = new ArrayList<>();
        File file = new File(DATA_FOLDER, fileName);

        if (!file.exists()) return records;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    records.add(line.split("\\|", -1));
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading " + fileName);
            e.printStackTrace();
        }
        return records;
    }

    public static void append(String fileName, String record) {
        File folder = new File(DATA_FOLDER);
        if (!folder.exists()) folder.mkdirs();

        try (PrintWriter out = new PrintWriter(new FileWriter(
                new File(folder, fileName), true))) {
            out.println(record.replace("\n", " ").replace("\r", " "));
        } catch (IOException e) {
            System.out.println("Error saving data to " + fileName);
            e.printStackTrace();
        }
    }

    public static int nextId(String fileName) {
        int max = 0;
        for (String[] row : read(fileName)) {
            try {
                max = Math.max(max, Integer.parseInt(row[0]));
            } catch (Exception ignored) { }
        }
        return max + 1;
    }

    public static boolean exists(String fileName) {
        return new File(DATA_FOLDER, fileName).exists();
    }
}
