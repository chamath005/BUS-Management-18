import java.util.List;
import java.util.Scanner;

public class viewbookigs {

    public static void viewBookings(Scanner input) {
        System.out.print("Enter Passenger ID: ");
        String passengerId = input.nextLine();

        List<String[]> bookings = FileHandling.read("bookings.txt");
        List<String[]> schedules = FileHandling.read("schedules.txt");
        List<String[]> buses = FileHandling.read("buses.txt");
        List<String[]> routes = FileHandling.read("routes.txt");
        List<String[]> seats = FileHandling.read("seats.txt");
        boolean found = false;

        System.out.println("\n========== MY BOOKINGS ==========");
        for (String[] b : bookings) {
            if (b.length < 6 || !b[1].equals(passengerId)) continue;
            String[] s = searchbuses.findById(schedules, b[2]);
            String[] seat = searchbuses.findById(seats, b[3]);
            if (s == null || seat == null) continue;
            String[] bus = searchbuses.findById(buses, s[1]);
            String[] route = searchbuses.findById(routes, s[2]);
            if (bus == null || route == null) continue;

            found = true;
            System.out.println("\nBooking ID: " + b[0]);
            System.out.println("Bus Number: " + bus[1]);
            System.out.println("Route: " + route[1] + " to " + route[2]);
            System.out.println("Seat Number: " + seat[2]);
            System.out.println("Departure: " + s[3]);
            System.out.println("Ticket Price: Rs. " + s[5]);
            System.out.println("Booking Date: " + b[4]);
            System.out.println("Status: " + b[5]);
            System.out.println("--------------------------------");
        }
        if (!found) System.out.println("No bookings found.");
    }
}
