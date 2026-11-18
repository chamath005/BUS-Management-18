
package owner.dashboard;

import java.util.Scanner;

import owner.filehandling.BusFile;
import owner.filehandling.RouteFile;
import owner.filehandling.TripFile;
import owner.filehandling.TicketFile;

import owner.model.Bus;
import owner.model.Route;
import owner.model.Trip;

public class OwnerDashboard {

    private Scanner input = new Scanner(System.in);

    private BusFile busFile = new BusFile();
    private RouteFile routeFile = new RouteFile();
    private TripFile tripFile = new TripFile();
    private TicketFile ticketFile = new TicketFile();

    public void displaymenu() {

        while (true) {

            System.out.println("\n===== BUS OWNER DASHBOARD =====");
            System.out.println("1. Bus Management");
            System.out.println("2. Route Management");
            System.out.println("3. Trip Management");
            System.out.println("4. View Passengers in Buses");

            System.out.println("5. Logout");

            System.out.print("Enter your choice: ");
            int choice = input.nextInt();

            switch (choice) {

                case 1:
                    busManagement();
                    break;

                case 2:
                    routeManagement();
                    break;

                case 3:
                    tripManagement();
                    break;

                case 4:
                    passengerManagement();
                    break;

                case 5:
                    System.out.println("Route Statistics");
                    break;

                case 6:
                    System.out.println("Bus Monitoring");
                    break;

                case 7:
                    System.out.println("Logging out...");
                    return;

                default:
                    System.out.println("Invalid choice!");
            }
        }
    }

    // ROUTE MANAGEMENT

    public void routeManagement() {

        while (true) {

            System.out.println("\n===== ROUTE MANAGEMENT =====");
            System.out.println("1. Add Route");
            System.out.println("2. View Routes");
            System.out.println("3. Update Route");
            System.out.println("4. Remove Route");
            System.out.println("5. Back");

            System.out.print("Enter your choice: ");
            int choice = input.nextInt();

            switch (choice) {

                case 1:
                    addRoute();
                    break;

                case 2:
                    routeFile.readRoutes();
                    break;

                case 3:
                    updateRoute();
                    break;

                case 4:
                    removeRoute();
                    break;

                case 5:
                    return;

                default:
                    System.out.println("Invalid choice!");
            }
        }
    }

    public void addRoute() {

        System.out.print("Enter Route ID: ");
        int routeId = input.nextInt();
        input.nextLine();

        System.out.print("Enter Starting Location: ");
        String startLocation = input.nextLine();

        System.out.print("Enter Ending Location: ");
        String endLocation = input.nextLine();

        System.out.print("Enter Distance: ");
        double distance = input.nextDouble();

        Route route = new Route(routeId, startLocation, endLocation, distance);

        routeFile.saveRoute(route);
    }

    public void updateRoute() {

        routeFile.readRoutes();

        System.out.print("Enter Route ID to update: ");
        int routeId = input.nextInt();
        input.nextLine();

        System.out.print("Enter New Starting Location: ");
        String startLocation = input.nextLine();

        System.out.print("Enter New Ending Location: ");
        String endLocation = input.nextLine();

        System.out.print("Enter New Distance: ");
        double distance = input.nextDouble();

        routeFile.updateRoute(routeId, startLocation, endLocation, distance);
    }

    public void removeRoute() {

        routeFile.readRoutes();

        System.out.print("Enter Route ID to remove: ");
        int routeId = input.nextInt();

        routeFile.removeRoute(routeId);
    }

    // BUS MANAGEMENT

    public void busManagement() {

        while (true) {

            System.out.println("\n===== BUS MANAGEMENT =====");
            System.out.println("1. Add Bus");
            System.out.println("2. View Buses");
            System.out.println("3. Update Bus");
            System.out.println("4. Remove Bus");
            System.out.println("5. Back");

            System.out.print("Enter your choice: ");
            int choice = input.nextInt();

            switch (choice) {

                case 1:
                    addBus();
                    break;

                case 2:
                    busFile.readBuses();
                    break;

                case 3:
                    updateBus();
                    break;

                case 4:
                    removeBus();
                    break;

                case 5:
                    return;

                default:
                    System.out.println("Invalid choice!");
            }
        }
    }

    public void addBus() {

        System.out.print("Enter Bus ID: ");
        int busId = input.nextInt();
        input.nextLine();

        System.out.print("Enter Registration Number: ");
        String registrationNumber = input.nextLine();

        System.out.print("Enter Bus Capacity: ");
        int capacity = input.nextInt();
        input.nextLine();

        System.out.print("Enter Bus Status: ");
        String status = input.nextLine();

        System.out.print("Enter Bus Speed: ");
        double speed = input.nextDouble();

        Bus bus = new Bus(busId, registrationNumber, capacity, status, speed);

        busFile.saveBus(bus);
    }

    public void updateBus() {

        busFile.readBuses();

        System.out.print("Enter Bus ID to update: ");
        int busId = input.nextInt();
        input.nextLine();

        System.out.print("Enter New Bus Status: ");
        String status = input.nextLine();

        System.out.print("Enter New Bus Speed: ");
        double speed = input.nextDouble();

        busFile.updateBus(busId, status, speed);
    }

    public void removeBus() {

        busFile.readBuses();

        System.out.print("Enter Bus ID to remove: ");
        int busId = input.nextInt();

        busFile.removeBus(busId);
    }

    // TRIP MANAGEMENT

    public void tripManagement() {

        while (true) {

            System.out.println("\n===== TRIP MANAGEMENT =====");
            System.out.println("1. Create a Trip");
            System.out.println("2. View All Trips");
            System.out.println("3. Back");

            System.out.print("Enter your choice: ");
            int choice = input.nextInt();

            switch (choice) {

                case 1:
                    createTrip();
                    break;

                case 2:
                    tripFile.readTrips();
                    break;

                case 3:
                    return;

                default:
                    System.out.println("Invalid choice!");
            }
        }
    }

    public void createTrip() {

        System.out.print("Enter Trip ID: ");
        int tripId = input.nextInt();

        System.out.print("Enter Bus ID: ");
        int busId = input.nextInt();

        Bus bus = busFile.findBus(busId);

        if (bus == null) {
            System.out.println("Bus not found!");
            return;
        }

        System.out.print("Enter Route ID: ");
        int routeId = input.nextInt();

        Route route = routeFile.findRoute(routeId);

        if (route == null) {
            System.out.println("Route not found!");
            return;
        }

        input.nextLine();

        System.out.print("Enter Departure Time: ");
        String departureTime = input.nextLine();

        System.out.print("Enter Trip Status: ");
        String status = input.nextLine();

        Trip trip = new Trip(tripId, bus, route, departureTime, status);

        tripFile.saveTrip(trip);
    }

    // PASSENGER MANAGEMENT

    public void passengerManagement() {

        while (true) {

            System.out.println("\n===== PASSENGER MANAGEMENT =====");
            System.out.println("1. View Passengers in a Bus");
            System.out.println("2. Back");

            System.out.print("Enter your choice: ");
            int choice = input.nextInt();

            switch (choice) {

                case 1:
                    viewPassengersInBus();
                    break;

                case 2:
                    return;

                default:
                    System.out.println("Invalid choice!");
            }
        }
    }

    public void viewPassengersInBus() {

        busFile.readBuses();

        System.out.print("Enter Bus ID: ");
        int busId = input.nextInt();

        Bus bus = busFile.findBus(busId);

        if (bus == null) {
            System.out.println("Bus not found!");
            return;
        }

        ticketFile.viewPassengersInBus(busId);
    }
}
