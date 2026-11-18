package conductor.service;

import conductor.model.Conductor;
import conductor.model.Ticket;
import java.util.ArrayList;
import java.util.List;
import conductor.model.LostItemReport;

public class ConductorService {

    private final Conductor conductor;
    private final List<Ticket> tickets = new ArrayList<>();
    private int nextTicketId = 1;
    private final List<LostItemReport> lostItemReports = new ArrayList<>();
    private int nextReportId = 1;

    public ConductorService(Conductor conductor) {
        if (conductor == null) {
            throw new IllegalArgumentException("Conductor is required.");
        }

        this.conductor = conductor;
    }

    private String validatePassengerId(String passengerId) {
        if (passengerId == null || passengerId.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Passenger ID cannot be empty."
            );
        }

        return passengerId.trim();
    }

    private Ticket findActiveTicket(String passengerId) {
        for (Ticket ticket : tickets) {
            if (ticket.getPassengerId().equals(passengerId)
                    && ticket.isActive()) {
                return ticket;
            }
        }

        return null;
    }

    public void recordBoarding(String passengerId) {
        conductor.recordBoarding(passengerId);
    }

    public void recordExiting(String passengerId) {
        passengerId = validatePassengerId(passengerId);

        conductor.recordExiting(passengerId);

        Ticket ticket = findActiveTicket(passengerId);

        if (ticket != null) {
            ticket.closeTicket();
        }
    }

    public Ticket issueTicket(String passengerId, int fare) {
        passengerId = validatePassengerId(passengerId);

        if (!conductor.getOnboardPassengerIds().contains(passengerId)) {
            throw new IllegalArgumentException(
                    "Passenger must board before receiving a ticket."
            );
        }

        if (fare <= 0) {
            throw new IllegalArgumentException(
                    "Fare must be greater than zero."
            );
        }

        if (findActiveTicket(passengerId) != null) {
            throw new IllegalArgumentException(
                    "This passenger already has a ticket."
            );
        }

        Ticket ticket = new Ticket(
                nextTicketId,
                passengerId,
                fare
        );

        tickets.add(ticket);
        nextTicketId++;

        return ticket;
    }

    public int getPassengerCount() {
        return conductor.getPassengerCount();
    }

    public int getAvailableSeats() {
        return conductor.getAvailableSeats();
    }

    public List<String> getPassengersWithoutTickets() {
        List<String> passengers = new ArrayList<>();

        for (String passengerId : conductor.getOnboardPassengerIds()) {
            if (findActiveTicket(passengerId) == null) {
                passengers.add(passengerId);
            }
        }

        return passengers;
    }

    public int getTicketCount() {
        return tickets.size();
    }

    public long getTotalRevenue() {
        long total = 0;

        for (Ticket ticket : tickets) {
            total += ticket.getFare();
        }

        return total;
    }

    public void viewTicketSales() {
        System.out.println("--- TICKET SALES ---");

        if (tickets.isEmpty()) {
            System.out.println("No tickets have been issued.");
        }

        for (Ticket ticket : tickets) {
            System.out.println(
                    "Ticket " + ticket.getTicketId()
                            + " | Passenger: " + ticket.getPassengerId()
                            + " | Fare: Rs. " + ticket.getFare()
            );
        }

        System.out.println("Tickets sold: " + getTicketCount());
        System.out.println("Total revenue: Rs. " + getTotalRevenue());
    }

    public double calculateCommission(double percentage) {
        if (Double.isNaN(percentage)
                || Double.isInfinite(percentage)
                || percentage < 0
                || percentage > 100) {

            throw new IllegalArgumentException(
                    "Commission percentage must be between 0 and 100."
            );
        }

        double commission = getTotalRevenue() * percentage / 100.0;

        return Math.round(commission * 100.0) / 100.0;
    }

    public LostItemReport reportLostItem(String passengerId,
                                         String description) {

        passengerId = validatePassengerId(passengerId);

        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Item description cannot be empty."
            );
        }

        LostItemReport report = new LostItemReport(
                nextReportId,
                passengerId,
                description.trim()
        );

        lostItemReports.add(report);
        nextReportId++;

        return report;
    }

    public void updateLostItemStatus(int reportId, String newStatus) {
        for (LostItemReport report : lostItemReports) {
            if (report.getReportId() == reportId) {
                report.updateStatus(newStatus);
                return;
            }
        }

        throw new IllegalArgumentException("Report ID was not found.");
    }

    public void viewLostItemReports() {
        System.out.println("--- LOST ITEM REPORTS ---");

        if (lostItemReports.isEmpty()) {
            System.out.println("No lost item reports.");
            return;
        }

        for (LostItemReport report : lostItemReports) {
            System.out.println(
                    "Report " + report.getReportId()
                            + " | Passenger: " + report.getPassengerId()
                            + " | Item: " + report.getDescription()
                            + " | Status: " + report.getStatus()
            );
        }
    }

    public Conductor getConductor() {
        return conductor;
    }

    public List<Ticket> getTickets() {
        return new ArrayList<>(tickets);
    }

    public List<LostItemReport> getLostItemReports() {
        return new ArrayList<>(lostItemReports);
    }

    public void restoreTicket(String passengerId, int fare, boolean active) {
        passengerId = validatePassengerId(passengerId);

        if (fare <= 0) {
            throw new IllegalArgumentException("Invalid saved fare.");
        }

        if (active && (!conductor.getOnboardPassengerIds().contains(passengerId)
                || findActiveTicket(passengerId) != null)) {
            throw new IllegalArgumentException("Invalid saved active ticket.");
        }

        Ticket ticket = new Ticket(nextTicketId, passengerId, fare);

        if (!active) {
            ticket.closeTicket();
        }

        tickets.add(ticket);
        nextTicketId++;
    }
}
