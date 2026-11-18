package driver.model;

public class Trip {
    private int tripId;
    private Bus bus;
    private Route route;
    private String departureTime;
    private String status;
    private String currentLocation;

    public Trip(int tripId, Bus bus, Route route,
                String departureTime, String status) {
        this.tripId = tripId;
        this.bus = bus;
        this.route = route;
        this.departureTime = departureTime;
        this.status = status;
        this.currentLocation = route == null ? "" : route.getStartLocation();
    }

    public int getTripId() { return tripId; }
    public Bus getBus() { return bus; }
    public Route getRoute() { return route; }
    public String getDepartureTime() { return departureTime; }
    public String getStatus() { return status; }
    public String getCurrentLocation() { return currentLocation; }

    public void setDepartureTime(String departureTime) { this.departureTime = departureTime; }
    public void setStatus(String status) { this.status = status; }
    public void setCurrentLocation(String currentLocation) { this.currentLocation = currentLocation; }
}
