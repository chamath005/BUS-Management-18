package driver.service;

import driver.model.Driver;
import java.util.ArrayList;
import java.util.List;

public class DriverService {
    private final List<Driver> drivers = new ArrayList<>();

    public void addDriver(Driver driver) {
        if (driver != null) drivers.add(driver);
    }

    public List<Driver> getDrivers() {
        return new ArrayList<>(drivers);
    }

    public Driver findDriverById(int driverId) {
        for (Driver driver : drivers) {
            if (driver.getUserId() == driverId) return driver;
        }
        return null;
    }

    public int getTotalDrivers() {
        return drivers.size();
    }
}
