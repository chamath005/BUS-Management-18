package owner;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import common.*;

import java.io.IOException;
import java.util.*;

public class OwnerHandler {
    public static void register(HttpServer server) {
        OwnerHandler handler = new OwnerHandler();
        server.createContext("/api/owner/", handler::handle);
    }

    private void handle(HttpExchange x) throws IOException {
        try {
            Map<String, Object> user = UnifiedAuth.requireRole(x, "owner");
            if (user == null) return;

            String path = x.getRequestURI().getPath();
            String method = x.getRequestMethod();
            Map<String, String> f = method.equals("POST") ? HttpSupport.form(x) : new HashMap<>();

            if (path.equals("/api/owner/summary")) {
                HttpSupport.json(x, 200, summary());
                return;
            }
            if (path.equals("/api/owner/data")) {
                HttpSupport.json(x, 200, Json.obj("success", true, "rows", data(HttpSupport.query(x, "section"))));
                return;
            }
            if (path.equals("/api/owner/save") && method.equals("POST")) {
                save(f);
                HttpSupport.json(x, 200, Json.obj("success", true));
                return;
            }
            if (path.equals("/api/owner/delete") && method.equals("POST")) {
                delete(f, String.valueOf(user.get("id")));
                HttpSupport.json(x, 200, Json.obj("success", true));
                return;
            }
            if (path.equals("/api/owner/add-user") && method.equals("POST")) {
                Map<String, Object> created = UnifiedAuth.createUserRecord(
                        HttpSupport.required(f, "name"),
                        HttpSupport.required(f, "email"),
                        HttpSupport.required(f, "password"),
                        HttpSupport.required(f, "role"),
                        f.getOrDefault("phone", ""),
                        f.getOrDefault("extra", ""),
                        f.getOrDefault("userId", "")
                );
                HttpSupport.json(x, 200, Json.obj("success", true, "message", "User added: " + created.get("name") + " (" + created.get("role") + ")", "user", created));
                return;
            }
            if (path.equals("/api/owner/assign-driver") && method.equals("POST")) {
                assignDriver(HttpSupport.required(f, "driverId"), HttpSupport.required(f, "tripId"));
                HttpSupport.json(x, 200, Json.obj("success", true, "message", "Driver assigned to trip successfully."));
                return;
            }
            if (path.equals("/api/owner/replacement-status") && method.equals("POST")) {
                updateReplacementStatus(f);
                HttpSupport.json(x, 200, Json.obj("success", true));
                return;
            }
            HttpSupport.json(x, 404, Json.obj("success", false, "message", "Owner endpoint not found."));
        } catch (IllegalArgumentException e) {
            HttpSupport.json(x, 400, Json.obj("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            HttpSupport.json(x, 500, Json.obj("success", false, "message", "Unable to complete request: " + e.getMessage()));
        }
    }

    private Map<String, Object> summary() throws IOException {
        int pending = 0;
        for (String l : SharedData.readLines("replacement_requests.txt")) {
            String[] p = SharedData.pipe(l);
            if (p.length >= 7 && p[6].equalsIgnoreCase("PENDING")) pending++;
        }
        return Json.obj(
                "success", true,
                "buses", count("buses.txt"),
                "routes", count("routes.txt"),
                "trips", count("trips.txt"),
                "users", count("users.txt"),
                "breakdowns", count("breakdown_reports.txt"),
                "accidents", count("accident_reports.txt"),
                "requests", count("replacement_requests.txt"),
                "pendingRequests", pending,
                "tripUpdates", count("trip_updates.txt")
        );
    }

    private int count(String file) throws IOException {
        int n = 0;
        for (String l : SharedData.readLines(file)) if (!l.isBlank()) n++;
        return n;
    }

    private List<Map<String, Object>> data(String section) throws IOException {
        List<Map<String, Object>> out = new ArrayList<>();
        String file = switch (section) {
            case "buses" -> "buses.txt";
            case "routes" -> "routes.txt";
            case "trips" -> "trips.txt";
            case "users" -> "users.txt";
            case "drivers" -> "drivers.txt";
            case "assignments" -> "driver_assignments.txt";
            case "passengers" -> "tickets.txt";
            case "tripUpdates" -> "trip_updates.txt";
            case "breakdowns" -> "breakdown_reports.txt";
            case "accidents" -> "accident_reports.txt";
            case "replacements" -> "replacement_requests.txt";
            case "locations" -> "location_updates.txt";
            default -> throw new IllegalArgumentException("Unknown section.");
        };

        for (String line : SharedData.readLines(file)) {
            if (line.isBlank()) continue;
            if (section.equals("drivers")) {
                String[] p = line.split(",", -1);
                if (p.length >= 6) {
                    out.add(Json.obj("driverId", p[0].trim(), "name", p[1].trim(), "email", p[2].trim(), "phone", p[4].trim(), "license", p[5].trim()));
                }
                continue;
            }
            String[] p = SharedData.pipe(line);
            switch (section) {
                case "buses" -> {
                    if (p.length >= 5) out.add(Json.obj("busId", p[0], "registrationNumber", p[1], "capacity", p[2], "status", p[3], "speed", p[4], "busType", p.length > 5 ? p[5] : ""));
                }
                case "routes" -> {
                    if (p.length >= 4) out.add(Json.obj("routeId", p[0], "startLocation", p[1], "endLocation", p[2], "distance", p[3]));
                }
                case "trips" -> {
                    if (p.length >= 5) out.add(Json.obj("tripId", p[0], "busId", p[1], "routeId", p[2], "departureTime", p[3], "status", p[4], "currentLocation", p.length > 5 ? p[5] : ""));
                }
                case "users" -> {
                    if (p.length >= 5) out.add(Json.obj("userId", p[0], "name", p[1], "email", p[2], "role", p[4], "phone", p.length > 5 ? p[5] : "", "extra", p.length > 6 ? p[6] : ""));
                }
                case "assignments" -> {
                    if (p.length >= 2) out.add(Json.obj("driverId", p[0], "driverName", driverName(p[0]), "tripId", p[1], "busNumber", busForTrip(p[1])));
                }
                case "passengers" -> {
                    if (p.length >= 5) out.add(Json.obj("ticketId", p[0], "passengerId", p[1], "tripId", p[2], "fare", p[3], "ticketTime", p[4], "passengerName", passengerName(p[1]), "busNumber", busForTrip(p[2])));
                }
                case "tripUpdates" -> {
                    if (p.length >= 7) out.add(Json.obj("driverId", p[0], "driverName", p[1], "tripId", p[2], "busNumber", p[3], "status", p[4], "location", p[5], "updatedAt", p[6]));
                }
                case "breakdowns" -> {
                    if (p.length >= 8) out.add(Json.obj("reportId", p[0], "driverId", p[1], "driverName", p[2], "busNumber", p[3], "category", p[4], "description", p[5], "severity", p[6], "date", p[7]));
                }
                case "accidents" -> {
                    if (p.length >= 8) out.add(Json.obj("reportId", p[0], "driverId", p[1], "driverName", p[2], "busNumber", p[3], "location", p[4], "description", p[5], "severity", p[6], "date", p[7]));
                }
                case "replacements" -> {
                    if (p.length >= 7) out.add(Json.obj("requestId", p[0], "driverId", p[1], "driverName", p[2], "busNumber", p[3], "reason", p[4], "date", p[5], "status", p[6]));
                }
                case "locations" -> {
                    if (p.length >= 6) out.add(Json.obj("driverId", p[0], "driverName", p[1], "tripId", p[2], "busNumber", p[3], "location", p[4], "updatedAt", p[5]));
                }
            }
        }
        return out;
    }

    private String driverName(String driverId) throws IOException {
        for (String l : SharedData.readLines("drivers.txt")) {
            String[] p = l.split(",", -1);
            if (p.length >= 2 && p[0].trim().equals(driverId)) return p[1].trim();
        }
        return "Driver #" + driverId;
    }

    private String passengerName(String id) throws IOException {
        for (String l : SharedData.readLines("users.txt")) {
            String[] p = SharedData.pipe(l);
            if (p.length >= 5 && p[0].equals(id)) return p[1];
        }
        for (String l : SharedData.readLines("passengers.txt")) {
            String[] p = SharedData.pipe(l);
            if (p.length >= 2 && p[0].equals(id)) return p[1];
        }
        return "Passenger #" + id;
    }

    private String busForTrip(String trip) throws IOException {
        for (String l : SharedData.readLines("trips.txt")) {
            String[] p = SharedData.pipe(l);
            if (p.length >= 2 && p[0].equals(trip)) {
                for (String b : SharedData.readLines("buses.txt")) {
                    String[] q = SharedData.pipe(b);
                    if (q.length >= 2 && q[0].equals(p[1])) return q[1];
                }
            }
        }
        return "";
    }

    private void assignDriver(String driverId, String tripId) throws IOException {
        if (!exists("trips.txt", tripId)) {
            throw new IllegalArgumentException("Trip ID #" + tripId + " does not exist.");
        }
        List<String> rows = SharedData.readLines("driver_assignments.txt");
        boolean updated = false;
        for (int i = 0; i < rows.size(); i++) {
            String[] p = SharedData.pipe(rows.get(i));
            if (p.length >= 2 && p[0].equals(driverId.trim())) {
                rows.set(i, driverId.trim() + "|" + tripId.trim());
                updated = true;
                break;
            }
        }
        if (!updated) rows.add(driverId.trim() + "|" + tripId.trim());
        SharedData.replaceLines("driver_assignments.txt", rows);
    }

    private void save(Map<String, String> f) throws Exception {
        String type = HttpSupport.required(f, "type");
        String file = switch (type) {
            case "buses" -> "buses.txt";
            case "routes" -> "routes.txt";
            case "trips" -> "trips.txt";
            default -> throw new IllegalArgumentException("Unsupported record type.");
        };
        String idKey = type.equals("buses") ? "busId" : type.equals("routes") ? "routeId" : "tripId";
        String id = HttpSupport.required(f, idKey);
        List<String> rows = SharedData.readLines(file);
        String replacement;

        if (type.equals("buses")) {
            int capacity = Integer.parseInt(f.getOrDefault("capacity", "40"));
            double speed = Double.parseDouble(f.getOrDefault("speed", "0"));
            replacement = String.join("|", id, SharedData.clean(HttpSupport.required(f, "registrationNumber")),
                    String.valueOf(capacity), SharedData.clean(f.getOrDefault("status", "Available")),
                    String.valueOf(speed), SharedData.clean(f.getOrDefault("busType", "Normal")));
        } else if (type.equals("routes")) {
            double distance = Double.parseDouble(f.getOrDefault("distance", "0"));
            replacement = String.join("|", id, SharedData.clean(HttpSupport.required(f, "startLocation")),
                    SharedData.clean(HttpSupport.required(f, "endLocation")), String.valueOf(distance));
        } else {
            String busId = HttpSupport.required(f, "busId"), routeId = HttpSupport.required(f, "routeId");
            if (!exists("buses.txt", busId) || !exists("routes.txt", routeId)) {
                throw new IllegalArgumentException("Choose an existing Bus ID and Route ID first.");
            }
            String oldLocation = "";
            for (String row : rows) {
                String[] p = SharedData.pipe(row);
                if (p.length >= 6 && p[0].equals(id)) oldLocation = p[5];
            }
            String depTime = SharedData.clean(HttpSupport.required(f, "departureTime"));
            String status = SharedData.clean(f.getOrDefault("status", "Scheduled"));
            replacement = String.join("|", id, busId, routeId, depTime, status, oldLocation);
            syncSchedule(id, busId, routeId, depTime);
        }

        boolean found = false;
        for (int i = 0; i < rows.size(); i++) {
            String[] p = SharedData.pipe(rows.get(i));
            if (p.length > 0 && p[0].equals(id)) {
                rows.set(i, replacement);
                found = true;
                break;
            }
        }
        if (!found) rows.add(replacement);
        SharedData.replaceLines(file, rows);
    }

    private void syncSchedule(String tripId, String busId, String routeId, String depTime) throws IOException {
        List<String> scheds = SharedData.readLines("schedules.txt");
        boolean found = false;
        for (String s : scheds) {
            String[] p = SharedData.pipe(s);
            if (p.length > 0 && p[0].equals(tripId)) { found = true; break; }
        }
        if (!found) {
            SharedData.append("schedules.txt", tripId, busId, routeId, depTime, "TBD", "1000.00", "Daily");
        }
    }

    private boolean exists(String file, String id) throws IOException {
        for (String l : SharedData.readLines(file)) {
            String[] p = SharedData.pipe(l);
            if (p.length > 0 && p[0].equals(id)) return true;
        }
        return false;
    }

    private void delete(Map<String, String> f, String currentUserId) throws Exception {
        String type = HttpSupport.required(f, "type"), id = HttpSupport.required(f, "id");
        if (type.equals("users") && id.equals(currentUserId)) {
            throw new IllegalArgumentException("You cannot delete your currently signed-in account.");
        }
        String file = switch (type) {
            case "buses" -> "buses.txt";
            case "routes" -> "routes.txt";
            case "trips" -> "trips.txt";
            case "users" -> "users.txt";
            default -> throw new IllegalArgumentException("Unsupported record type.");
        };
        List<String> rows = SharedData.readLines(file);
        boolean removed = rows.removeIf(l -> {
            String[] p = SharedData.pipe(l);
            return p.length > 0 && p[0].equals(id);
        });
        if (!removed) throw new IllegalArgumentException("Record not found.");
        SharedData.replaceLines(file, rows);
    }

    private void updateReplacementStatus(Map<String, String> f) throws IOException {
        String reqId = HttpSupport.required(f, "requestId");
        String status = HttpSupport.required(f, "status");
        List<String> rows = SharedData.readLines("replacement_requests.txt");
        for (int i = 0; i < rows.size(); i++) {
            String[] p = SharedData.pipe(rows.get(i));
            if (p.length >= 7 && p[0].equals(reqId)) {
                p[6] = SharedData.clean(status);
                rows.set(i, String.join("|", p));
                break;
            }
        }
        SharedData.replaceLines("replacement_requests.txt", rows);
    }
}
