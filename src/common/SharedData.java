package common;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/** Shared text-file persistence used by Owner, Driver, Conductor, and Passenger modules. */
public final class SharedData {
    private static final Path DATA_DIR = resolveDataDirectory();
    private SharedData() {}

    private static Path resolveDataDirectory() {
        String configured = System.getProperty("busmanager.dataDir");
        if (configured != null && !configured.isBlank()) {
            return Paths.get(configured).toAbsolutePath().normalize();
        }
        Path working = Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();
        Path direct = working.resolve("shared-data").normalize();
        if (Files.isDirectory(direct) || Files.isDirectory(working.resolve("web"))) {
            return direct;
        }
        Path parent = working.getParent();
        if (parent != null && (Files.isDirectory(parent.resolve("shared-data")) || Files.isDirectory(parent.resolve("web")))) {
            return parent.resolve("shared-data").normalize();
        }
        return direct;
    }

    public static Path getDataDirectory() {
        return DATA_DIR;
    }

    public static void initialize() throws IOException {
        Files.createDirectories(DATA_DIR);

        // Unified user directory: userId|name|email|password|role|phone|extra
        createIfMissing("users.txt",
                "1|Chamath|test@gmail.com|1234|owner|0770000001|\n" +
                "2|Kamal|kamal@gmail.com|12345|driver|0771234567|B123456\n" +
                "845|Gimhan|gimhan@example.com|demo123|conductor|0771234567|CON001\n" +
                "201|Nimal|passenger@gmail.com|1234|passenger|0779876543|\n");

        // Driver records: driverId,name,email,password,phone,license
        createIfMissing("drivers.txt",
                "1,Kamal,kamal@gmail.com,12345,0771234567,B123456\n" +
                "2,Nimal,nimal@gmail.com,67890,0711234567,B654321\n");

        // Passenger records: passengerId|name|email|password|phone
        createIfMissing("passengers.txt",
                "201|Nimal|passenger@gmail.com|1234|0779876543\n");

        // Buses: busId|registrationNumber|capacity|status|speed|busType
        createIfMissing("buses.txt",
                "1|NA-1234|45|Available|60.0|Normal\n" +
                "1001|ND-3456|40|Available|65.0|AC\n" +
                "1002|NB-8365|50|Available|55.0|Normal\n");

        // Routes: routeId|startLocation|endLocation|distance
        createIfMissing("routes.txt",
                "1|Colombo|Kandy|120.5\n" +
                "101|Colombo|Galle|119.0\n" +
                "102|Colombo|Matara|160.0\n");

        // Trips: tripId|busId|routeId|departureTime|status|currentLocation
        createIfMissing("trips.txt",
                "1|1|1|08:00 AM|Scheduled|Colombo\n" +
                "19|1001|101|04:00 AM|Scheduled|Colombo\n" +
                "43|1002|102|05:00 AM|Scheduled|Colombo\n");

        createIfMissing("driver_assignments.txt", "1|19\n2|43\n");

        // Schedules for passenger booking: scheduleId|busId|routeId|departureTime|arrivalTime|ticketPrice|operatingDays
        createIfMissing("schedules.txt",
                "1|1|1|08:00 AM|11:30 AM|850.00|Daily\n" +
                "19|1001|101|04:00 AM|05:30 AM|1200.00|Daily\n" +
                "43|1002|102|05:00 AM|09:00 AM|1000.00|Daily\n");

        // Seats: seatId|busId|seatNumber
        StringBuilder defaultSeats = new StringBuilder();
        int seatSeq = 1;
        for (int busId : new int[]{1, 1001, 1002}) {
            for (int s = 1; s <= 12; s++) {
                defaultSeats.append(seatSeq++).append("|").append(busId).append("|S").append(s).append("\n");
            }
        }
        createIfMissing("seats.txt", defaultSeats.toString());

        createIfMissing("bookings.txt", "");
        createIfMissing("passenger_tickets.txt", "");
        createIfMissing("tickets.txt", "1|201|1|850.0|08:15 AM\n");
        createIfMissing("ratings.txt", "201|1001|5|Clean AC bus and punctual departure!\n");
        createIfMissing("complaints.txt", "");
        createIfMissing("lostitems.txt", "");
        createIfMissing("trip_updates.txt", "");
        createIfMissing("location_updates.txt", "");
        createIfMissing("breakdown_reports.txt", "");
        createIfMissing("accident_reports.txt", "");
        createIfMissing("replacement_requests.txt", "");
    }

    private static void createIfMissing(String name, String content) throws IOException {
        Path file = DATA_DIR.resolve(name);
        if (!Files.exists(file)) {
            Files.writeString(file, content, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
        }
    }

    public static List<String> readLines(String name) throws IOException {
        Path file = DATA_DIR.resolve(name);
        if (!Files.exists(file)) return new ArrayList<>();
        return new ArrayList<>(Files.readAllLines(file, StandardCharsets.UTF_8));
    }

    public static void append(String name, String... fields) throws IOException {
        String line = String.join("|", Arrays.stream(fields).map(SharedData::clean).toList()) + System.lineSeparator();
        appendRaw(name, line);
    }

    public static void appendCsv(String name, String... fields) throws IOException {
        String line = String.join(",", Arrays.stream(fields).map(SharedData::clean).toList()) + System.lineSeparator();
        appendRaw(name, line);
    }

    private static synchronized void appendRaw(String name, String text) throws IOException {
        Files.createDirectories(DATA_DIR);
        Path target = DATA_DIR.resolve(name);
        Path lockPath = DATA_DIR.resolve(name + ".lock");
        try (FileChannel lockChannel = FileChannel.open(lockPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
             FileLock ignored = lockChannel.lock();
             FileChannel channel = FileChannel.open(target, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND)) {
            ByteBuffer buffer = StandardCharsets.UTF_8.encode(text);
            while (buffer.hasRemaining()) channel.write(buffer);
            channel.force(true);
        }
    }

    public static synchronized void replaceLines(String name, List<String> rows) throws IOException {
        Files.createDirectories(DATA_DIR);
        Path target = DATA_DIR.resolve(name);
        Path lockPath = DATA_DIR.resolve(name + ".lock");
        try (FileChannel lockChannel = FileChannel.open(lockPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
             FileLock ignored = lockChannel.lock()) {
            Path temp = Files.createTempFile(DATA_DIR, name, ".tmp");
            Files.write(temp, rows, StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING);
            try {
                Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    public static String[] pipe(String row) {
        return row.split("\\|", -1);
    }

    public static String clean(String value) {
        if (value == null) return "";
        return value.replace('|', '/').replace(',', ' ').replace('\r', ' ').replace('\n', ' ').trim();
    }

    public static int nextId(String file, String delimiter) {
        int max = 0;
        try {
            for (String row : readLines(file)) {
                if (row.isBlank()) continue;
                String[] parts = row.split(java.util.regex.Pattern.quote(delimiter), -1);
                try {
                    max = Math.max(max, Integer.parseInt(parts[0].trim()));
                } catch (Exception ignored) {}
            }
        } catch (IOException ignored) {}
        return max + 1;
    }
}
