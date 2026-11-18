import java.util.Scanner;

public class test {

    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        passengerreg registration = new passengerreg();
        int choice = -1;

        while (choice != 0) {
            System.out.println("\n==============================");
            System.out.println("       PASSENGER PORTAL");
            System.out.println("==============================");
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("0. Exit");
            System.out.println("==============================");
            System.out.print("Enter your choice: ");

            try {
                choice = Integer.parseInt(input.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid Choice!");
                continue;
            }

            if (choice == 1) {
                System.out.print("Enter Name: ");
                String name = input.nextLine().trim();
                System.out.print("Enter Email: ");
                String email = input.nextLine().trim();
                System.out.print("Enter Password: ");
                String password = input.nextLine();
                System.out.print("Enter Phone: ");
                String phone = input.nextLine().trim();

                if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                    System.out.println("Name, email and password are required.");
                } else if (registration.registerPassenger(name, email, password, phone)) {
                    System.out.println("Registration successful. Please login.");
                } else {
                    System.out.println("This email is already registered.");
                }
            } else if (choice == 2) {
                System.out.print("Enter Email: ");
                String email = input.nextLine().trim();
                System.out.print("Enter Password: ");
                String password = input.nextLine();

                String[] user = registration.login(email, password);
                if (user == null) {
                    System.out.println("Invalid passenger email or password.");
                } else {
                    passengerdashboard.currentPassengerId = user[0];
                    System.out.println("Welcome " + user[1] + "! Your Passenger ID is " + user[0] + ".");
                    passengerdashboard.displayMenu(input);
                    passengerdashboard.currentPassengerId = "";
                }
            } else if (choice == 0) {
                System.out.println("Thank you!");
            } else {
                System.out.println("Invalid Choice!");
            }
        }
    }
}