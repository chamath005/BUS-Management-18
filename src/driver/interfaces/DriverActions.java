package driver.interfaces;

import driver.exceptions.InvalidTripStatusException;
import driver.model.BreakdownReport;
import driver.model.AccidentReport;
import driver.model.ReplacementRequest;

public interface DriverActions {
    void viewAssignedBus();
    void viewRoute();
    void updateTripStatus(String status) throws InvalidTripStatusException;
    void reportBreakdown(BreakdownReport report);
    void reportAccident(AccidentReport report);
    void requestReplacementBus(ReplacementRequest request);
}
