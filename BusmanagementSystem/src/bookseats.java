import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

public class bookseats {

    public static void bookSeat(Scanner input) {
        System.out.println("\n========== BOOK A SEAT ==========");
        System.out.print("Enter Starting Location: ");
        String start = input.nextLine();
        System.out.print("Enter Destination: ");
        String destination = input.nextLine();
        System.out.print("Enter Travel Date (YYYY-MM-DD): ");
        String date = input.nextLine();
        System.out.print("Enter Travel Day: ");
        String day = input.nextLine();

        List<String[]> buses = FileHandling.read("buses.txt");
        List<String[]> routes = FileHandling.read("routes.txt");
        List<String[]> schedules = FileHandling.read("schedules.txt");
        boolean found = false;

        System.out.println("\n========== AVAILABLE BUSES ==========");
        for (String[] s : schedules) {
            if (s.length < 7) continue;
            String[] bus = searchbuses.findById(buses, s[1]);
            String[] route = searchbuses.findById(routes, s[2]);
            if (bus != null && route != null
                    && route[1].equalsIgnoreCase(start)
                    && route[2].equalsIgnoreCase(destination)
                    && (s[6].equalsIgnoreCase("Daily")
                    || s[6].toLowerCase().contains(day.toLowerCase()))) {
                found = true;
                searchbuses.printBus(bus, route, s);
                System.out.println("Schedule ID: " + s[0]);
            }
        }
        if (!found) {
            System.out.println("No buses available for this route.");
            return;
        }

        System.out.print("\nEnter Passenger ID: ");
        String passengerId = input.nextLine();
        System.out.print("Enter Schedule ID: ");
        String scheduleId = input.nextLine();

        String[] selectedSchedule = searchbuses.findById(schedules, scheduleId);
        if (selectedSchedule == null) {
            System.out.println("Invalid Schedule ID!");
            return;
        }

        List<String[]> seats = FileHandling.read("seats.txt");
        List<String[]> bookings = FileHandling.read("bookings.txt");
        boolean seatsFound = false;
        System.out.println("\n========== AVAILABLE SEATS ==========");

        for (String[] seat : seats) {
            if (seat.length < 3 || !seat[1].equals(selectedSchedule[1])) continue;
            boolean booked = false;
            for (String[] booking : bookings) {
                if (booking.length >= 4 && booking[2].equals(scheduleId)
                        && booking[3].equals(seat[0])
                        && booking.length > 5 && booking[5].equalsIgnoreCase("Confirmed")) {
                    booked = true;
                    break;
                }
            }
            if (!booked) {
                seatsFound = true;
                System.out.println(seat[2]);
            }
        }

        if (!seatsFound) {
            System.out.println("No available seats!");
            return;
        }

        System.out.print("\nEnter Seat Number: ");
        String seatNumber = input.nextLine();
        String[] selectedSeat = null;

        for (String[] seat : seats) {
            if (seat.length >= 3 && seat[1].equals(selectedSchedule[1])
                    && seat[2].equalsIgnoreCase(seatNumber)) {
                selectedSeat = seat;
                break;
            }
        }

        if (selectedSeat == null) {
            System.out.println("Invalid Seat Number!");
            return;
        }

        for (String[] booking : bookings) {
            if (booking.length >= 6 && booking[2].equals(scheduleId)
                    && booking[3].equals(selectedSeat[0])
                    && booking[5].equalsIgnoreCase("Confirmed")) {
                System.out.println("This seat is already booked!");
                return;
            }
        }

        int bookingId = FileHandling.nextId("bookings.txt");
        String bookingDate = LocalDateTime.now().toString();
        FileHandling.append("bookings.txt", bookingId + "|" + passengerId + "|"
                + scheduleId + "|" + selectedSeat[0] + "|" + bookingDate + "|Confirmed");

        String ticketNumber = "TKT-" + bookingId;
        FileHandling.append("tickets.txt", bookingId + "|" + ticketNumber + "|" + bookingDate);

        System.out.println("\nSeat Booked Successfully!");
        System.out.println("Ticket Generated Successfully!");
        System.out.println("Ticket Number: " + ticketNumber);
    }
}
