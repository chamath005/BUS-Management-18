package driver;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import common.*;
import driver.model.*;
import driver.service.DriverAuthenticationService;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class DriverHandler {
    public static void register(HttpServer server) {
        DriverHandler handler = new DriverHandler();
        server.createContext("/api/driver/", handler::handle);
    }

    private void handle(HttpExchange x) throws IOException {
        try {
            Map<String, Object> user = UnifiedAuth.requireRole(x, "driver");
            if (user == null) return;

            Driver d = resolveDriver(user);
            String path = x.getRequestURI().getPath();
            String method = x.getRequestMethod();
            Map<String, String> f = method.equals("POST") ? HttpSupport.form(x) : new HashMap<>();

            if (path.equals("/api/driver/dashboard")) {
                HttpSupport.json(x, 200, dashboard(d));
                return;
            }
            if (path.equals("/api/driver/assign-trip") && method.equals("POST")) {
                int tripId = Integer.parseInt(HttpSupport.required(f, "tripId"));
                Trip t = DriverAuthenticationService.loadTrip(tripId);
                if (t == null) throw new IllegalArgumentException("Selected trip was not found.");
                saveAssignment(d.getUserId(), tripId);
                HttpSupport.json(x, 200, Json.obj("success", true, "message", "Assigned to Trip #" + tripId + "."));
                return;
            }
            if (path.equals("/api/driver/trip") && method.equals("POST")) {
                updateTrip(d, f);
                HttpSupport.json(x, 200, Json.obj("success", true, "message", "Trip status and current location saved."));
                return;
            }
            if (path.equals("/api/driver/breakdown") && method.equals("POST")) {
                saveBreakdown(d, f);
                HttpSupport.json(x, 200, Json.obj("success", true, "message", "Breakdown report submitted to the Owner."));
                return;
            }
            if (path.equals("/api/driver/accident") && method.equals("POST")) {
                saveAccident(d, f);
                HttpSupport.json(x, 200, Json.obj("success", true, "message", "Accident report submitted to the Owner."));
                return;
            }
            if (path.equals("/api/driver/replacement") && method.equals("POST")) {
                saveReplacement(d, f);
                HttpSupport.json(x, 200, Json.obj("success", true, "message", "Replacement bus request sent to the Owner."));
                return;
            }
            HttpSupport.json(x, 404, Json.obj("success", false, "message", "Driver endpoint not found."));
        } catch (IllegalArgumentException e) {
            HttpSupport.json(x, 400, Json.obj("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            HttpSupport.json(x, 500, Json.obj("success", false, "message", "Unable to complete request: " + e.getMessage()));
        }
    }

    private Driver resolveDriver(Map<String, Object> user) throws IOException {
        String email = String.valueOf(user.get("email"));
        String name = String.valueOf(user.get("name"));
        for (String line : SharedData.readLines("drivers.txt")) {
            if (line.isBlank()) continue;
            String[] p = line.split(",", -1);
            if (p.length >= 6 && (p[2].equalsIgnoreCase(email) || p[1].equalsIgnoreCase(name))) {
                Driver d = new Driver(Integer.parseInt(p[0].trim()), p[1].trim(), p[2].trim(), p[3].trim(), p[4].trim(), p[5].trim());
                Integer tripId = ensureAssignment(d.getUserId());
                if (tripId != null) d.setAssignedTrip(DriverAuthenticationService.loadTrip(tripId));
                return d;
            }
        }
        int id = Integer.parseInt(String.valueOf(user.get("id")));
        Driver d = new Driver(id, name, email, "", String.valueOf(user.getOrDefault("phone", "")), String.valueOf(user.getOrDefault("extra", "LIC-" + id)));
        Integer tripId = ensureAssignment(d.getUserId());
        if (tripId != null) d.setAssignedTrip(DriverAuthenticationService.loadTrip(tripId));
        return d;
    }

    private void saveAssignment(int driverId, int tripId) throws IOException {
        List<String> rows = SharedData.readLines("driver_assignments.txt");
        boolean updated = false;
        for (int i = 0; i < rows.size(); i++) {
            String[] p = SharedData.pipe(rows.get(i));
            if (p.length >= 2 && p[0].equals(String.valueOf(driverId))) {
                rows.set(i, driverId + "|" + tripId);
                updated = true;
                break;
            }
        }
        if (!updated) rows.add(driverId + "|" + tripId);
        SharedData.replaceLines("driver_assignments.txt", rows);
    }

    private Integer ensureAssignment(int driverId) throws IOException {
        Integer existing = assignment(driverId);
        if (existing != null) return existing;
        for (String line : SharedData.readLines("trips.txt")) {
            if (line.isBlank()) continue;
            String[] p = SharedData.pipe(line);
            if (p.length >= 1) {
                int firstTripId = Integer.parseInt(p[0].trim());
                saveAssignment(driverId, firstTripId);
                return firstTripId;
            }
        }
        return null;
    }

    private List<Map<String, Object>> availableTrips() throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (String line : SharedData.readLines("trips.txt")) {
            if (line.isBlank()) continue;
            String[] p = SharedData.pipe(line);
            if (p.length >= 5) {
                int tid = Integer.parseInt(p[0].trim());
                Trip t = DriverAuthenticationService.loadTrip(tid);
                String label = "Trip #" + tid;
                if (t != null && t.getBus() != null && t.getRoute() != null) {
                    label += " · " + t.getBus().getRegistrationNumber() + " (" + t.getRoute().getStartLocation() + " → " + t.getRoute().getEndLocation() + " @ " + t.getDepartureTime() + ")";
                }
                list.add(Json.obj("tripId", tid, "label", label));
            }
        }
        return list;
    }

    private Map<String, Object> dashboard(Driver d) throws IOException {
        Integer id = ensureAssignment(d.getUserId());
        Trip t = id == null ? null : DriverAuthenticationService.loadTrip(id);
        if (t != null) d.setAssignedTrip(t);
        Bus b = d.getAssignedBus();
        Route r = t == null ? null : t.getRoute();
        return Json.obj(
                "success", true,
                "driverId", d.getUserId(),
                "licenseNumber", d.getLicenseNumber(),
                "availableTrips", availableTrips(),
                "bus", b == null ? null : Json.obj("busId", b.getBusId(), "registrationNumber", b.getRegistrationNumber(), "capacity", b.getCapacity(), "status", b.getStatus(), "speed", b.getSpeed(), "busType", b.getBusType()),
                "route", r == null ? null : Json.obj("routeId", r.getRouteId(), "startLocation", r.getStartLocation(), "endLocation", r.getEndLocation(), "distance", r.getDistance()),
                "trip", t == null ? null : Json.obj("tripId", t.getTripId(), "departureTime", t.getDepartureTime(), "status", t.getStatus(), "currentLocation", t.getCurrentLocation())
        );
    }

    private Integer assignment(int driverId) throws IOException {
        for (String line : SharedData.readLines("driver_assignments.txt")) {
            String[] p = SharedData.pipe(line);
            if (p.length >= 2 && p[0].equals(String.valueOf(driverId))) return Integer.parseInt(p[1].trim());
        }
        return null;
    }

    private Trip assignedTrip(Driver d) throws IOException {
        Integer id = ensureAssignment(d.getUserId());
        if (id == null) throw new IllegalArgumentException("No trips exist in the system yet.");
        Trip t = DriverAuthenticationService.loadTrip(id);
        if (t == null) throw new IllegalArgumentException("Your assigned trip could not be found.");
        d.setAssignedTrip(t);
        return t;
    }

    private void updateTrip(Driver d, Map<String, String> f) throws Exception {
        String location = HttpSupport.required(f, "location"), status = HttpSupport.required(f, "status");
        Trip t = assignedTrip(d);
        d.updateTripStatus(status);
        List<String> rows = SharedData.readLines("trips.txt");
        boolean found = false;
        for (int i = 0; i < rows.size(); i++) {
            String[] p = SharedData.pipe(rows.get(i));
            if (p.length >= 5 && p[0].equals(String.valueOf(t.getTripId()))) {
                String[] n = Arrays.copyOf(p, Math.max(6, p.length));
                n[4] = t.getStatus();
                n[5] = SharedData.clean(location);
                rows.set(i, String.join("|", n));
                found = true;
                break;
            }
        }
        if (!found) throw new IllegalArgumentException("Trip record could not be found.");
        SharedData.replaceLines("trips.txt", rows);
        String bus = t.getBus().getRegistrationNumber(), now = LocalDateTime.now().toString();
        SharedData.append("trip_updates.txt", String.valueOf(d.getUserId()), d.getName(), String.valueOf(t.getTripId()), bus, t.getStatus(), location, now);
        SharedData.append("location_updates.txt", String.valueOf(d.getUserId()), d.getName(), String.valueOf(t.getTripId()), bus, location, now);
    }

    private void saveBreakdown(Driver d, Map<String, String> f) throws Exception {
        Trip t = assignedTrip(d);
        String category = HttpSupport.required(f, "category"), desc = HttpSupport.required(f, "description"), severity = HttpSupport.required(f, "severity");
        String id = "BR-" + System.currentTimeMillis();
        BreakdownReport r = new BreakdownReport(id, d.getUserId(), d.getName(), t.getBus().getRegistrationNumber(), category, desc, severity, LocalDate.now().toString());
        d.reportBreakdown(r);
        SharedData.append("breakdown_reports.txt", id, String.valueOf(d.getUserId()), d.getName(), t.getBus().getRegistrationNumber(), category, desc, severity, LocalDate.now().toString());
    }

    private void saveAccident(Driver d, Map<String, String> f) throws Exception {
        Trip t = assignedTrip(d);
        String loc = HttpSupport.required(f, "location"), desc = HttpSupport.required(f, "description"), sev = HttpSupport.required(f, "severity"), id = "AR-" + System.currentTimeMillis();
        AccidentReport r = new AccidentReport(id, d.getUserId(), d.getName(), t.getBus().getRegistrationNumber(), loc, desc, sev, LocalDate.now().toString());
        d.reportAccident(r);
        SharedData.append("accident_reports.txt", id, String.valueOf(d.getUserId()), d.getName(), t.getBus().getRegistrationNumber(), loc, desc, sev, LocalDate.now().toString());
    }

    private void saveReplacement(Driver d, Map<String, String> f) throws Exception {
        Trip t = assignedTrip(d);
        String reason = HttpSupport.required(f, "reason"), id = "RR-" + System.currentTimeMillis();
        ReplacementRequest r = new ReplacementRequest(id, d.getUserId(), d.getName(), t.getBus().getRegistrationNumber(), reason, LocalDate.now().toString(), "PENDING");
        d.requestReplacementBus(r);
        SharedData.append("replacement_requests.txt", id, String.valueOf(d.getUserId()), d.getName(), t.getBus().getRegistrationNumber(), reason, LocalDate.now().toString(), "PENDING");
    }
}
