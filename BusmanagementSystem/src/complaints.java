import java.time.LocalDateTime;
import java.util.Scanner;

public class complaints {

    public static void submitComplaint(Scanner input) {
        System.out.println("\n========== SUBMIT COMPLAINT ==========");
        System.out.print("Enter Passenger ID: ");
        String passengerId = input.nextLine();
        System.out.print("Enter Your Complaint: ");
        String description = input.nextLine();

        int complaintId = FileHandling.nextId("complaints.txt");
        FileHandling.append("complaints.txt", complaintId + "|" + passengerId
                + "|" + description + "|" + LocalDateTime.now());
        System.out.println("\nComplaint Submitted Successfully!");
    }
}
