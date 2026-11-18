package conductor.interfaces;

public interface ConductorActions {

    void recordBoarding(String passengerId);

    void recordExiting(String passengerId);

    int getPassengerCount();

    int getAvailableSeats();
}