package driver.model;

public class Bus {
    private int busId;
    private String registrationNumber;
    private int capacity;
    private String status;
    private double speed;
    private String busType;

    public Bus(int busId, String registrationNumber,
               int capacity, String status, double speed) {
        this(busId, registrationNumber, capacity, status, speed, "Unknown");
    }

    public Bus(int busId, String registrationNumber, int capacity,
               String status, double speed, String busType) {
        this.busId = busId;
        this.registrationNumber = registrationNumber;
        this.capacity = capacity;
        this.status = status;
        this.speed = speed;
        this.busType = busType;
    }

    public int getBusId() { return busId; }
    public String getRegistrationNumber() { return registrationNumber; }
    public int getCapacity() { return capacity; }
    public String getStatus() { return status; }
    public double getSpeed() { return speed; }
    public String getBusType() { return busType; }

    public void setStatus(String status) { this.status = status; }
    public void setSpeed(double speed) { this.speed = speed; }
}
