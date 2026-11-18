package driver.model;

public class ReplacementRequest {
    private String requestId;
    private int driverId;
    private String driverName;
    private String busNumber;
    private String reason;
    private String requestDate;
    private String status;

    public ReplacementRequest(String requestId, int driverId, String driverName,
                              String busNumber, String reason,
                              String requestDate, String status) {
        this.requestId = requestId;
        this.driverId = driverId;
        this.driverName = driverName;
        this.busNumber = busNumber;
        this.reason = reason;
        this.requestDate = requestDate;
        this.status = status;
    }

    public String getRequestId() { return requestId; }
    public int getDriverId() { return driverId; }
    public String getDriverName() { return driverName; }
    public String getBusNumber() { return busNumber; }
    public String getReason() { return reason; }
    public String getRequestDate() { return requestDate; }
    public String getStatus() { return status; }

    public void displayRequest() {
        System.out.println("\n----- REPLACEMENT BUS REQUEST -----");
        System.out.println("Request ID: " + requestId);
        System.out.println("Driver ID: " + driverId);
        System.out.println("Driver Name: " + driverName);
        System.out.println("Current Bus Number: " + busNumber);
        System.out.println("Reason: " + reason);
        System.out.println("Request Date: " + requestDate);
        System.out.println("Status: " + status);
    }
}
