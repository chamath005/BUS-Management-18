package conductor.model;

import conductor.interfaces.ConductorActions;
import java.util.ArrayList;
import java.util.List;

public class Conductor extends User implements ConductorActions {

    private String employeeId;
    private final int seatCapacity;
    private final List<String> onboardPassengerIds = new ArrayList<>();

    public Conductor(int userId, String name, String email,
                     String password, String phone,
                     String employeeId, int seatCapacity) {

        super(userId, name, email, password, phone);

        if (seatCapacity <= 0) {
            throw new IllegalArgumentException(
                    "Seat capacity must be greater than zero."
            );
        }

        this.employeeId = employeeId;
        this.seatCapacity = seatCapacity;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public int getSeatCapacity() {
        return seatCapacity;
    }

    private String validatePassengerId(String passengerId) {
        if (passengerId == null || passengerId.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Passenger ID cannot be empty."
            );
        }

        return passengerId.trim();
    }

    @Override
    public void recordBoarding(String passengerId) {
        passengerId = validatePassengerId(passengerId);

        if (onboardPassengerIds.contains(passengerId)) {
            throw new IllegalArgumentException(
                    "This passenger is already inside the bus."
            );
        }

        if (getAvailableSeats() == 0) {
            throw new IllegalArgumentException(
                    "No seats are available."
            );
        }

        onboardPassengerIds.add(passengerId);
    }

    @Override
    public void recordExiting(String passengerId) {
        passengerId = validatePassengerId(passengerId);

        if (!onboardPassengerIds.remove(passengerId)) {
            throw new IllegalArgumentException(
                    "This passenger is not inside the bus."
            );
        }
    }

    @Override
    public int getPassengerCount() {
        return onboardPassengerIds.size();
    }

    @Override
    public int getAvailableSeats() {
        return seatCapacity - getPassengerCount();
    }

    public List<String> getOnboardPassengerIds() {
        return new ArrayList<>(onboardPassengerIds);
    }

    @Override
    public void displayRole() {
        System.out.println("User Role: Conductor");
    }

    @Override
    public void viewProfile() {
        super.viewProfile();
        System.out.println("Employee ID: " + employeeId);
    }
}