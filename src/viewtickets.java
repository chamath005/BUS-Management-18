import java.util.List;
import java.util.Scanner;

public class viewtickets {

    public static void viewTickets(Scanner input) {
        String passengerId = passengerdashboard.currentPassengerId;

        List<String[]> tickets = FileHandling.read("tickets.txt");
        List<String[]> bookings = FileHandling.read("bookings.txt");
        List<String[]> schedules = FileHandling.read("schedules.txt");
        List<String[]> buses = FileHandling.read("buses.txt");
        List<String[]> routes = FileHandling.read("routes.txt");
        List<String[]> seats = FileHandling.read("seats.txt");
        boolean found = false;

        System.out.println("\n========== MY TICKETS ==========");
        for (String[] t : tickets) {
            if (t.length < 5 || !t[1].equals(passengerId)) continue;
            String[] b = searchbuses.findById(bookings, t[0]);
            if (b != null && b.length >= 6 && !b[5].equalsIgnoreCase("Confirmed")) continue;
            String[] s = searchbuses.findById(schedules, t[2]);
            String[] seat = b == null ? null : searchbuses.findById(seats, b[3]);
            String[] bus = s == null ? null : searchbuses.findById(buses, s[1]);
            String[] route = s == null ? null : searchbuses.findById(routes, s[2]);

            found = true;
            System.out.println("\nTicket Number: TKT-" + t[0]);
            System.out.println("Bus Number: " + (bus == null ? "N/A" : bus[1]));
            System.out.println("Route: " + (route == null ? "N/A" : route[1] + " to " + route[2]));
            System.out.println("Seat Number: " + (seat == null ? "N/A" : seat[2]));
            System.out.println("Departure: " + (s == null ? "N/A" : s[3]));
            System.out.println("Ticket Price: Rs. " + t[3]);
            System.out.println("Issue Date: " + t[4]);
            System.out.println("--------------------------------");
        }
        if (!found) System.out.println("No tickets found.");
    }
}
