import java.util.List;
import java.util.Scanner;

public class viewticketprice {

    public static void viewPrices(Scanner input) {
        System.out.println("\n========== TICKET PRICES ==========");
        System.out.print("Enter Starting Location: ");
        String start = input.nextLine();
        System.out.print("Enter Destination: ");
        String destination = input.nextLine();

        List<String[]> buses = FileHandling.read("buses.txt");
        List<String[]> routes = FileHandling.read("routes.txt");
        List<String[]> schedules = FileHandling.read("schedules.txt");
        boolean found = false;

        System.out.println("\n========== AVAILABLE TICKET PRICES ==========");
        for (String[] s : schedules) {
            if (s.length < 7) continue;
            String[] bus = searchbuses.findById(buses, s[1]);
            String[] route = searchbuses.findById(routes, s[2]);
            if (bus != null && route != null
                    && route[1].equalsIgnoreCase(start)
                    && route[2].equalsIgnoreCase(destination)) {
                found = true;
                searchbuses.printBus(bus, route, s);
                System.out.println("--------------------------------");
            }
        }
        if (!found) System.out.println("No ticket prices available for this route.");
    }
}
