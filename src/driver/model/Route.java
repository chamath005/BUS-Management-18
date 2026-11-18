package driver.model;

public class Route {
    private int routeId;
    private String startLocation;
    private String endLocation;
    private double distance;

    public Route(int routeId, String startLocation,
                 String endLocation, double distance) {
        this.routeId = routeId;
        this.startLocation = startLocation;
        this.endLocation = endLocation;
        this.distance = distance;
    }

    public int getRouteId() { return routeId; }
    public String getStartLocation() { return startLocation; }
    public String getEndLocation() { return endLocation; }
    public double getDistance() { return distance; }

    public void setStartLocation(String startLocation) { this.startLocation = startLocation; }
    public void setEndLocation(String endLocation) { this.endLocation = endLocation; }
    public void setDistance(double distance) { this.distance = distance; }
}
