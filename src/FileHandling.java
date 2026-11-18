import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class FileHandling {

    public static final String DATA_FOLDER = resolveDataFolder().toString();

    private static Path resolveDataFolder() {
        String configured = System.getProperty("busmanager.dataDir");
        if (configured != null && !configured.isBlank()) {
            return Paths.get(configured).toAbsolutePath().normalize();
        }
        Path working = Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();
        Path parent = working.getParent();
        if (parent == null) parent = working;
        return parent.resolve("shared-data").normalize();
    }

    public static List<String[]> read(String fileName) {
        switch (fileName) {
            case "buses.txt": return readBuses();
            case "schedules.txt": return readSchedules();
            case "seats.txt": return readSeats();
            default: return readRaw(fileName);
        }
    }

    private static List<String[]> readRaw(String fileName) {
        List<String[]> records = new ArrayList<>();
        Path file = Paths.get(DATA_FOLDER, fileName);

        if (!Files.exists(file)) return records;

        try {
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
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

    private static List<String[]> readBuses() {
        List<String[]> buses = new ArrayList<>();
        for (String[] b : readRaw("buses.txt")) {
            if (b.length < 5) continue;
            String type = b.length > 5 && !b[5].isBlank() ? b[5] : "Not specified";
            buses.add(new String[]{b[0], b[1], type, b[2]});
        }
        return buses;
    }

    private static List<String[]> readSchedules() {
        List<String[]> schedules = new ArrayList<>();
        List<String[]> trips = readRaw("trips.txt");
        for (String[] d : readRaw("bus_schedule_details.txt")) {
            if (d.length < 9 || d[0].equals("schedule_id")) continue;
            for (String[] trip : trips) {
                if (trip.length >= 5 && trip[0].equals(d[0])) {
                    schedules.add(new String[]{d[0], trip[1], trip[2], d[5], d[6], d[7], d[8],
                            trip[4], trip.length > 5 ? trip[5] : ""});
                    break;
                }
            }
        }
        return schedules;
    }

    private static List<String[]> readSeats() {
        List<String[]> seats = new ArrayList<>();
        for (String[] b : readRaw("buses.txt")) {
            if (b.length < 5) continue;
            int capacity;
            try {
                capacity = Integer.parseInt(b[2].trim());
            } catch (NumberFormatException e) {
                continue;
            }
            for (int number = 1; number <= capacity; number++) {
                seats.add(new String[]{b[0] + "-" + number, b[0], String.valueOf(number)});
            }
        }
        return seats;
    }

    public static void append(String fileName, String record) {
        try {
            Path folder = Paths.get(DATA_FOLDER);
            Files.createDirectories(folder);
            String line = record.replace("\n", " ").replace("\r", " ") + System.lineSeparator();
            try (FileChannel lockChannel = FileChannel.open(folder.resolve(fileName + ".lock"),
                    StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                 FileLock ignored = lockChannel.lock();
                 BufferedWriter out = Files.newBufferedWriter(folder.resolve(fileName), StandardCharsets.UTF_8,
                         StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
                out.write(line);
            }
        } catch (IOException e) {
            System.out.println("Error saving data to " + fileName);
            e.printStackTrace();
        }
    }

    public static int nextId(String fileName) {
        int max = 0;
        for (String[] row : read(fileName)) {
            try {
                max = Math.max(max, Integer.parseInt(row[0].trim()));
            } catch (Exception ignored) { }
        }
        return max + 1;
    }

    public static boolean exists(String fileName) {
        return Files.exists(Paths.get(DATA_FOLDER, fileName));
    }
}
