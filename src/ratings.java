import java.util.List;
import java.util.Scanner;

public class ratings {

    public static void giveRating(Scanner input) {
        System.out.println("\n GIVE RATING ");
        String passengerId = passengerdashboard.currentPassengerId;
        System.out.print("Enter Bus Number: ");
        String busNumber = input.nextLine();
        System.out.print("Enter Rating (1-5): ");
        int rating = Integer.parseInt(input.nextLine());

        if (rating < 1 || rating > 5) {
            System.out.println("Invalid Rating! Enter 1 to 5.");
            return;
        }

        System.out.print("Enter Your Comment: ");
        String comment = input.nextLine();

        List<String[]> buses = FileHandling.read("buses.txt");
        String[] selectedBus = null;
        for (String[] bus : buses) {
            if (bus.length >= 3 && bus[1].equalsIgnoreCase(busNumber)) {
                selectedBus = bus;
                break;
            }
        }

        if (selectedBus == null) {
            System.out.println("Bus not found!");
            return;
        }

        FileHandling.append("ratings.txt",
                passengerId + "|" + selectedBus[0] + "|" + rating + "|" + comment);
        System.out.println("Rating Submitted Successfully!");
    }
}
