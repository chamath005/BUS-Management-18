import java.util.List;
import java.util.Scanner;

public class viewfeedbacks {

    public static void viewFeedback(Scanner input) {
        System.out.println("\nVIEW FEEDBACK ");
        System.out.print("Enter Bus Number: ");
        String busNumber = input.nextLine();

        List<String[]> buses = FileHandling.read("buses.txt");
        List<String[]> passengers = FileHandling.read("users.txt");
        List<String[]> ratings = FileHandling.read("ratings.txt");
        String[] selectedBus = null;

        for (String[] bus : buses) {
            if (bus.length >= 2 && bus[1].equalsIgnoreCase(busNumber)) {
                selectedBus = bus;
                break;
            }
        }

        boolean found = false;
        if (selectedBus != null) {
            for (String[] r : ratings) {
                if (r.length < 4 || !r[1].equals(selectedBus[0])) continue;
                String passengerName = "Unknown";
                for (String[] p : passengers) {
                    if (p.length >= 2 && p[0].equals(r[0])) {
                        passengerName = p[1];
                        break;
                    }
                }
                found = true;
                System.out.println("\nPassenger: " + passengerName);
                System.out.println("Rating: " + r[2] + "/5");
                System.out.println("Comment: " + r[3]);
                System.out.println("----------------------------");
            }
        }

        if (!found) System.out.println("No feedback found for this bus.");
    }
}
