package owner.model;

public class Ticket {
    private int ticketId;
    private int passengerId;
    private Trip trip;
    private double fare;
    private String ticketTime;

    public Ticket(int ticketId,int passengerId,Trip trip,double fare,String ticketTime){
        this.ticketId=ticketId;
        this.passengerId=passengerId;
        this.trip=trip;
        this.fare=fare;
        this.ticketTime=ticketTime;}

    public int getTicketId() {
        return ticketId;
    }

    public int getPassengerId() {
        return passengerId;
    }

    public Trip getTrip() {
        return trip;
    }

    public double getFare() {
        return fare;
    }

    public String getTicketTime() {
        return ticketTime;
    }

    public void setFare(double fare) {
        this.fare = fare;
    }
}

