package driver.model;

public class AccidentReport {
    private String reportId;
    private int driverId;
    private String driverName;
    private String busNumber;
    private String location;
    private String description;
    private String severity;
    private String reportDate;

    public AccidentReport(String reportId, int driverId, String driverName,
                          String busNumber, String location, String description,
                          String severity, String reportDate) {
        this.reportId = reportId;
        this.driverId = driverId;
        this.driverName = driverName;
        this.busNumber = busNumber;
        this.location = location;
        this.description = description;
        this.severity = severity;
        this.reportDate = reportDate;
    }

    public String getReportId() { return reportId; }
    public int getDriverId() { return driverId; }
    public String getDriverName() { return driverName; }
    public String getBusNumber() { return busNumber; }
    public String getLocation() { return location; }
    public String getDescription() { return description; }
    public String getSeverity() { return severity; }
    public String getReportDate() { return reportDate; }

    public void displayReport() {
        System.out.println("\n----- ACCIDENT REPORT -----");
        System.out.println("Report ID: " + reportId);
        System.out.println("Driver ID: " + driverId);
        System.out.println("Driver Name: " + driverName);
        System.out.println("Bus Number: " + busNumber);
        System.out.println("Accident Location: " + location);
        System.out.println("Description: " + description);
        System.out.println("Severity: " + severity);
        System.out.println("Report Date: " + reportDate);
    }
}
