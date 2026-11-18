import java.util.Scanner;

public class passengerdashboard {

    public static void displayMenu() {
        Scanner input = new Scanner(System.in);
        int choice = -1;

        while (choice != 0) {
            System.out.println("\n==============================");
            System.out.println("     PASSENGER DASHBOARD");
            System.out.println("==============================");
            System.out.println("1. Search Buses");
            System.out.println("2. View Bus schedules");
            System.out.println("3. View Ticket Prices");
            System.out.println("4. Book a Seat");
            System.out.println("5. View My Bookings");
            System.out.println("6. View My Tickets");
            System.out.println("7. Report Lost Items");
            System.out.println("8. Give Rating");
            System.out.println("9. View Feedbacks");
            System.out.println("10. Submit complaints");
            System.out.println("0. Exit");
            System.out.println("==============================");
            System.out.print("Enter your choice: ");

            choice = Integer.parseInt(input.nextLine());

            switch (choice) {
                case 1: searchbuses.searchBuses(input); break;
                case 2: viewschedules.viewSchedules(); break;
                case 3: viewticketprice.viewPrices(input); break;
                case 4: bookseats.bookSeat(input); break;
                case 5: viewbookigs.viewBookings(input); break;
                case 6: viewtickets.viewTickets(input); break;
                case 7: lostitems.reportLostItem(input); break;
                case 8: ratings.giveRating(input); break;
                case 9: viewfeedbacks.viewFeedback(input); break;
                case 10: complaints.submitComplaint(input); break;
                case 0: System.out.println("Thank you!"); break;
                default: System.out.println("Invalid Choice!");
            }
        }
    }
}
