import java.util.List;
import java.util.Scanner;

public class searchbuses {

    public static void searchBuses(Scanner input) {
        System.out.println("\n========== BUS SEARCH ==========");

        System.out.print("Enter Starting Location: ");
        String start = input.nextLine();
        System.out.print("Enter Destination: ");
        String destination = input.nextLine();
        System.out.print("Enter Travel Date (YYYY-MM-DD): ");
        String date = input.nextLine();
        System.out.print("Enter Travel Day (Monday, Tuesday, etc.): ");
        String day = input.nextLine();

        List<String[]> buses = FileHandling.read("buses.txt");
        List<String[]> routes = FileHandling.read("routes.txt");
        List<String[]> schedules = FileHandling.read("schedules.txt");
        boolean found = false;

        System.out.println("\n========== AVAILABLE BUSES ==========");

        for (String[] s : schedules) {
            if (s.length < 7) continue;
            String[] bus = findById(buses, s[1]);
            String[] route = findById(routes, s[2]);

            if (bus == null || route == null) continue;

            boolean routeMatch = route[1].equalsIgnoreCase(start)
                    && route[2].equalsIgnoreCase(destination);
            boolean dayMatch = s[6].equalsIgnoreCase("Daily")
                    || s[6].toLowerCase().contains(day.toLowerCase());

            if (routeMatch && dayMatch) {
                found = true;
                printBus(bus, route, s);
            }
        }

        if (!found) System.out.println("No buses found for this route and date.");
    }

    static String[] findById(List<String[]> rows, String id) {
        for (String[] row : rows) {
            if (row.length > 0 && row[0].equals(id)) return row;
        }
        return null;
    }

    static void printBus(String[] bus, String[] route, String[] schedule) {
        System.out.println("\nBus Number: " + bus[1]);
        System.out.println("Bus Type: " + bus[2]);
        System.out.println("Route: " + route[1] + " to " + route[2]);
        System.out.println("Departure: " + schedule[3]);
        System.out.println("Arrival: " + schedule[4]);
        System.out.println("Ticket Price: Rs. " + schedule[5]);
        if (schedule.length > 7 && !schedule[7].isBlank()) {
            System.out.println("Trip Status: " + schedule[7]);
        }
        if (schedule.length > 8 && !schedule[8].isBlank()) {
            System.out.println("Current Location: " + schedule[8]);
        }
    }
}
