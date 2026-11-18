package conductor.model;

public class LostItemReport {

    private final int reportId;
    private final String passengerId;
    private final String description;
    private String status;

    public LostItemReport(int reportId, String passengerId,
                          String description) {

        this.reportId = reportId;
        this.passengerId = passengerId;
        this.description = description;
        this.status = "REPORTED";
    }

    public int getReportId() {
        return reportId;
    }

    public String getPassengerId() {
        return passengerId;
    }

    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return status;
    }

    public void updateStatus(String newStatus) {
        if (newStatus == null) {
            throw new IllegalArgumentException("Status is required.");
        }

        newStatus = newStatus.trim();

        if (status.equals("REPORTED")
                && newStatus.equalsIgnoreCase("FOUND")) {

            status = "FOUND";

        } else if (status.equals("FOUND")
                && newStatus.equalsIgnoreCase("RETURNED")) {

            status = "RETURNED";

        } else {
            throw new IllegalArgumentException(
                    "Allowed changes: REPORTED to FOUND, FOUND to RETURNED."
            );
        }
    }
}