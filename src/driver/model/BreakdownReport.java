package driver.model;

public class BreakdownReport {
    private String reportId;
    private int driverId;
    private String driverName;
    private String busNumber;
    private String category;
    private String description;
    private String severity;
    private String reportDate;

    public BreakdownReport(String reportId, int driverId, String driverName,
                           String busNumber, String category, String description,
                           String severity, String reportDate) {
        this.reportId = reportId;
        this.driverId = driverId;
        this.driverName = driverName;
        this.busNumber = busNumber;
        this.category = category;
        this.description = description;
        this.severity = severity;
        this.reportDate = reportDate;
    }

    public String getReportId() { return reportId; }
    public int getDriverId() { return driverId; }
    public String getDriverName() { return driverName; }
    public String getBusNumber() { return busNumber; }
    public String getCategory() { return category; }
    public String getDescription() { return description; }
    public String getSeverity() { return severity; }
    public String getReportDate() { return reportDate; }

    public void displayReport() {
        System.out.println("\n----- BREAKDOWN REPORT -----");
        System.out.println("Report ID: " + reportId);
        System.out.println("Driver ID: " + driverId);
        System.out.println("Driver Name: " + driverName);
        System.out.println("Bus Number: " + busNumber);
        System.out.println("Issue: " + category);
        System.out.println("Description: " + description);
        System.out.println("Severity: " + severity);
        System.out.println("Report Date: " + reportDate);
    }
}
