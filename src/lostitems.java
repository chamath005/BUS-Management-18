import java.time.LocalDateTime;
import java.util.Scanner;

public class lostitems {

    public static void reportLostItem(Scanner input) {
        System.out.println("\n========== REPORT LOST ITEM ==========");
        String passengerId = passengerdashboard.currentPassengerId;
        System.out.print("Enter Lost Item Description: ");
        String description = input.nextLine();
        System.out.print("Enter Travel Date (YYYY-MM-DD): ");
        String travelDate = input.nextLine();

        int reportId = FileHandling.nextId("lostitems.txt");
        FileHandling.append("lostitems.txt", reportId + "|" + passengerId + "|"
                + description + "|" + travelDate + "|" + LocalDateTime.now() + "|REPORTED");
        System.out.println("\nLost Item Report Submitted Successfully!");
    }
}
