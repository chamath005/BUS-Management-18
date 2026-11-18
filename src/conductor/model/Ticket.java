package conductor.model;

public class Ticket {

    private final int ticketId;
    private final String passengerId;
    private final int fare;
    private boolean active;

    public Ticket(int ticketId, String passengerId, int fare) {
        this.ticketId = ticketId;
        this.passengerId = passengerId;
        this.fare = fare;
        this.active = true;
    }

    public int getTicketId() {
        return ticketId;
    }

    public String getPassengerId() {
        return passengerId;
    }

    public int getFare() {
        return fare;
    }

    public boolean isActive() {
        return active;
    }

    public void closeTicket() {
        active = false;
    }
}