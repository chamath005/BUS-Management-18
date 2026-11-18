package driver.model;

import driver.interfaces.DriverActions;
import driver.exceptions.InvalidTripStatusException;

public class Driver extends User implements DriverActions {
    private String licenseNumber;
    private Bus assignedBus;
    private Trip assignedTrip;

    public Driver(int userId, String name, String email, String password,
                  String phone, String licenseNumber) {
        super(userId, name, email, password, phone);
        this.licenseNumber = licenseNumber;
    }

    public Driver(int userId, String name, String email, String password,
                  String phone, String licenseNumber, Bus assignedBus) {
        this(userId, name, email, password, phone, licenseNumber);
        this.assignedBus = assignedBus;
    }

    public Driver(int userId, String name, String email, String password,
                  String phone, String licenseNumber, Trip assignedTrip) {
        this(userId, name, email, password, phone, licenseNumber);
        setAssignedTrip(assignedTrip);
    }

    public String getLicenseNumber() { return licenseNumber; }
    public Bus getAssignedBus() { return assignedBus; }
    public Trip getAssignedTrip() { return assignedTrip; }

    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }
    public void setAssignedBus(Bus assignedBus) { this.assignedBus = assignedBus; }

    public void setAssignedTrip(Trip assignedTrip) {
        this.assignedTrip = assignedTrip;
        this.assignedBus = assignedTrip == null ? null : assignedTrip.getBus();
    }

    @Override
    public void viewAssignedBus() {
        Bus bus = assignedBus;
        if (assignedTrip != null) bus = assignedTrip.getBus();

        if (bus == null) {
            System.out.println("No bus is assigned to this driver.");
            return;
        }

        System.out.println("\n----- ASSIGNED BUS DETAILS -----");
        System.out.println("Bus ID: " + bus.getBusId());
        System.out.println("Bus Number: " + bus.getRegistrationNumber());
        if (bus.getBusType() != null && !bus.getBusType().equals("Unknown")) {
            System.out.println("Bus Type: " + bus.getBusType());
        }
        System.out.println("Capacity: " +
                (bus.getCapacity() <= 0 ? "Not provided in timetable" : bus.getCapacity()));
        System.out.println("Status: " + bus.getStatus());
        System.out.println("Speed: " +
                (bus.getSpeed() <= 0 ? "Not provided in timetable" : bus.getSpeed()));
    }

    @Override
    public void viewRoute() {
        if (assignedTrip == null || assignedTrip.getRoute() == null) {
            System.out.println("No route is assigned to this driver.");
            return;
        }

        Route route = assignedTrip.getRoute();
        System.out.println("\n----- ASSIGNED ROUTE DETAILS -----");
        System.out.println("Trip ID: " + assignedTrip.getTripId());
        System.out.println("Route ID: " + route.getRouteId());
        System.out.println("Start Location: " + route.getStartLocation());
        System.out.println("End Location: " + route.getEndLocation());
        System.out.println("Departure Time: " + assignedTrip.getDepartureTime());
        System.out.println("Current Location: " + assignedTrip.getCurrentLocation());
        System.out.println("Trip Status: " + assignedTrip.getStatus());
        if (route.getDistance() > 0) {
            System.out.println("Distance: " + route.getDistance() + " KM");
        } else {
            System.out.println("Distance: Not provided in timetable");
        }
    }

    @Override
    public void updateTripStatus(String status) throws InvalidTripStatusException {
        if (assignedTrip == null) {
            throw new InvalidTripStatusException("No trip is assigned to this driver.");
        }
        if (status == null) {
            throw new InvalidTripStatusException("Trip status cannot be empty.");
        }

        String normalized;
        if (status.equalsIgnoreCase("Started") || status.equalsIgnoreCase("ONGOING")) {
            normalized = "Ongoing";
        } else if (status.equalsIgnoreCase("Completed")) {
            normalized = "Completed";
        } else if (status.equalsIgnoreCase("Cancelled")) {
            normalized = "Cancelled";
        } else {
            throw new InvalidTripStatusException(
                    "Invalid status. Use Started, Completed, or Cancelled.");
        }

        assignedTrip.setStatus(normalized);
        System.out.println("Trip status updated: " + normalized);
    }

    @Override
    public void reportBreakdown(BreakdownReport report) {
        if (report == null) {
            System.out.println("Invalid breakdown report.");
            return;
        }
        System.out.println("Breakdown report submitted and saved for the Owner.");
        report.displayReport();
    }

    @Override
    public void reportAccident(AccidentReport report) {
        if (report == null) {
            System.out.println("Invalid accident report.");
            return;
        }
        System.out.println("Accident report submitted and saved for the Owner.");
        report.displayReport();
    }

    @Override
    public void requestReplacementBus(ReplacementRequest request) {
        if (request == null) {
            System.out.println("Invalid replacement bus request.");
            return;
        }
        System.out.println("Replacement bus request sent to the Owner successfully.");
        request.displayRequest();
    }

    @Override
    public void displayRole() {
        System.out.println("User Role: Driver");
    }
}
