
package passenger;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import common.*;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

public class PassengerHandler {

    public static void register(HttpServer server) {
        PassengerHandler handler = new PassengerHandler();
        server.createContext("/api/passenger/", handler::handle);
    }

    private void handle(HttpExchange x) throws IOException {
        try {
            Map<String, Object> user =
                    UnifiedAuth.requireRole(x, "passenger");

            if (user == null) return;

            String path = x.getRequestURI().getPath();
            String method = x.getRequestMethod();

            if ("GET".equals(method)) {
                Map<String, String> q =
                        parseParams(x.getRequestURI().getRawQuery());

                switch (path) {
                    case "/api/passenger/dashboard":
                        send(x, 200, dashboard(user));
                        return;

                    case "/api/passenger/search":
                    case "/api/passenger/schedules":
                    case "/api/passenger/prices":
                        send(x, 200, searchBuses(q));
                        return;

                    case "/api/passenger/seats":
                        send(x, 200, availableSeats(q));
                        return;

                    case "/api/passenger/bookings":
                        send(x, 200, bookings(user));
                        return;

                    case "/api/passenger/tickets":
                        send(x, 200, tickets(user));
                        return;

                    case "/api/passenger/feedback":
                        send(x, 200, feedback(q));
                        return;

                    default:
                        send(x, 404,
                                result(false, "Passenger endpoint not found."));
                        return;
                }
            }

            if ("POST".equals(method)) {
                Map<String, String> data = readBody(x);

                switch (path) {
                    case "/api/passenger/book":
                        send(x, 200, bookSeat(user, data));
                        return;

                    case "/api/passenger/lost-items":
                        send(x, 200, reportLostItem(user, data));
                        return;

                    case "/api/passenger/ratings":
                        send(x, 200, giveRating(user, data));
                        return;

                    case "/api/passenger/complaints":
                        send(x, 200, submitComplaint(user, data));
                        return;

                    default:
                        send(x, 404,
                                result(false, "Passenger endpoint not found."));
                        return;
                }
            }

            send(x, 405, result(false, "Method not allowed."));

        } catch (Exception e) {
            e.printStackTrace();
            send(x, 500, result(false,
                    "Server error: " + e.getMessage()));
        }
    }

    // 1. PASSENGER DASHBOARD

    private Map<String, Object> dashboard(
            Map<String, Object> user) throws IOException {

        String passengerId = String.valueOf(user.get("id"));

        long bookingCount = SharedData.readLines("bookings.txt")
                .stream()
                .filter(line -> {
                    String[] p = SharedData.pipe(line);
                    return p.length >= 2 &&
                            p[1].equals(passengerId);
                }).count();

        long ticketCount = SharedData.readLines("tickets.txt")
                .stream()
                .filter(line -> {
                    String[] p = SharedData.pipe(line);
                    return p.length >= 2 &&
                            p[1].equals(passengerId);
                }).count();

        return Json.obj(
                "success", true,
                "passengerId", passengerId,
                "passengerName", user.get("name"),
                "email", user.get("email"),
                "bookingCount", bookingCount,
                "ticketCount", ticketCount
        );
    }

    // 2. SEARCH BUSES
    // 3. VIEW SCHEDULES
    // 4. VIEW TICKET PRICES

    private Map<String, Object> searchBuses(
            Map<String, String> q) throws IOException {

        String start = value(q, "start").toLowerCase();
        String destination = value(q, "destination").toLowerCase();
        String day = value(q, "day").toLowerCase();

        List<Map<String, Object>> results = new ArrayList<>();

        for (String line : SharedData.readLines("schedules.txt")) {

            if (line.isBlank()) continue;

            String[] s = SharedData.pipe(line);
            if (s.length < 7) continue;

            String[] bus = find("buses.txt", s[1]);
            String[] route = find("routes.txt", s[2]);

            if (bus == null || bus.length < 3 ||
                    route == null || route.length < 3) {
                continue;
            }

            if (!start.isBlank() &&
                    !route[1].toLowerCase().contains(start)) {
                continue;
            }

            if (!destination.isBlank() &&
                    !route[2].toLowerCase().contains(destination)) {
                continue;
            }

            if (!day.isBlank() && !day.equals("any") &&
                    !s[6].equalsIgnoreCase("Daily") &&
                    !s[6].toLowerCase().contains(day)) {
                continue;
            }

            results.add(scheduleObject(s, bus, route));
        }

        return Json.obj(
                "success", true,
                "buses", results
        );
    }

    private Map<String, Object> scheduleObject(
            String[] s, String[] bus, String[] route) {

        return Json.obj(
                "scheduleId", s[0],
                "busId", s[1],
                "busNumber", bus[1],
                "capacity", bus[2],
                "busStatus", bus.length > 3 ? bus[3] : "",
                "busType", bus.length > 5 ? bus[5] : "Not specified",
                "routeId", s[2],
                "startLocation", route[1],
                "destination", route[2],
                "departure", s[3],
                "arrival", s[4],
                "ticketPrice", s[5],
                "operatingDays", s[6]
        );
    }

    // 5. BOOK SEATS

    private Map<String, Object> availableSeats(
            Map<String, String> q) throws IOException {

        String scheduleId = value(q, "scheduleId");
        String[] schedule = find("schedules.txt", scheduleId);

        if (schedule == null || schedule.length < 7) {
            return result(false, "Schedule not found.");
        }

        Set<String> booked = new HashSet<>();

        for (String line : SharedData.readLines("bookings.txt")) {
            String[] b = SharedData.pipe(line);

            if (b.length >= 6 &&
                    b[2].equals(scheduleId) &&
                    b[5].equalsIgnoreCase("Confirmed")) {
                booked.add(b[3]);
            }
        }

        List<Map<String, Object>> seats = new ArrayList<>();

        for (String line : SharedData.readLines("seats.txt")) {
            String[] s = SharedData.pipe(line);

            if (s.length >= 3 &&
                    s[1].equals(schedule[1]) &&
                    !booked.contains(s[0])) {

                seats.add(Json.obj(
                        "seatId", s[0],
                        "seatNumber", s[2]
                ));
            }
        }

        return Json.obj(
                "success", true,
                "seats", seats
        );
    }

    private Map<String, Object> bookSeat(
            Map<String, Object> user,
            Map<String, String> data) throws IOException {

        String passengerId = String.valueOf(user.get("id"));
        String scheduleId = value(data, "scheduleId");
        String seatId = value(data, "seatId");
        String bookingDate = value(data, "bookingDate");

        if (scheduleId.isBlank() ||
                seatId.isBlank() ||
                bookingDate.isBlank()) {
            return result(false, "Please fill all booking details.");
        }

        String[] schedule = find("schedules.txt", scheduleId);

        if (schedule == null || schedule.length < 7) {
            return result(false, "Schedule not found.");
        }

        String[] seat = find("seats.txt", seatId);

        if (seat == null || seat.length < 3) {
            return result(false, "Seat not found.");
        }

        if (!seat[1].equals(schedule[1])) {
            return result(false, "This seat does not belong to this bus.");
        }

        for (String line : SharedData.readLines("bookings.txt")) {
            String[] b = SharedData.pipe(line);

            if (b.length >= 6 &&
                    b[2].equals(scheduleId) &&
                    b[3].equals(seatId) &&
                    b[5].equalsIgnoreCase("Confirmed")) {

                return result(false, "This seat is already booked.");
            }
        }

        // Keep booking/ticket IDs unique even when tickets.txt already
        // contains older sample tickets without matching booking records.
        int nextBookingId = SharedData.nextId("bookings.txt", "|");
        int nextTicketId = SharedData.nextId("tickets.txt", "|");
        String bookingId = String.valueOf(
                Math.max(nextBookingId, nextTicketId));

        SharedData.append(
                "bookings.txt",
                bookingId,
                passengerId,
                scheduleId,
                seatId,
                bookingDate,
                "Confirmed"
        );

        SharedData.append(
                "tickets.txt",
                bookingId,
                passengerId,
                scheduleId,
                schedule[5],
                bookingDate
        );

        return Json.obj(
                "success", true,
                "message", "Seat booked successfully!",
                "ticketNumber", "TKT-" + bookingId
        );
    }

    // 6. VIEW BOOKINGS

    private Map<String, Object> bookings(
            Map<String, Object> user) throws IOException {

        String passengerId = String.valueOf(user.get("id"));

        List<Map<String, Object>> list = new ArrayList<>();

        for (String line : SharedData.readLines("bookings.txt")) {

            String[] b = SharedData.pipe(line);

            if (b.length < 6 || !b[1].equals(passengerId)) {
                continue;
            }

            String[] schedule = find("schedules.txt", b[2]);
            String[] seat = find("seats.txt", b[3]);

            String[] bus = schedule == null
                    ? null : find("buses.txt", schedule[1]);

            String[] route = schedule == null
                    ? null : find("routes.txt", schedule[2]);

            list.add(Json.obj(
                    "bookingId", b[0],
                    "scheduleId", b[2],
                    "seatNumber", seat == null ? b[3] : seat[2],
                    "busNumber", bus == null ? "" : bus[1],
                    "startLocation", route == null ? "" : route[1],
                    "destination", route == null ? "" : route[2],
                    "bookingDate", b[4],
                    "status", b[5]
            ));
        }

        return Json.obj(
                "success", true,
                "bookings", list
        );
    }

    // 7. VIEW TICKETS

    private Map<String, Object> tickets(
            Map<String, Object> user) throws IOException {

        String passengerId = String.valueOf(user.get("id"));

        List<Map<String, Object>> list = new ArrayList<>();

        for (String line : SharedData.readLines("tickets.txt")) {

            String[] t = SharedData.pipe(line);

            if (t.length < 5 || !t[1].equals(passengerId)) {
                continue;
            }

            String[] booking = find("bookings.txt", t[0]);

            if (booking != null && booking.length >= 6 &&
                    !booking[5].equalsIgnoreCase("Confirmed")) {
                continue;
            }

            String[] schedule = find("schedules.txt", t[2]);

            String[] seat = booking == null
                    ? null : find("seats.txt", booking[3]);

            String[] bus = schedule == null
                    ? null : find("buses.txt", schedule[1]);

            String[] route = schedule == null
                    ? null : find("routes.txt", schedule[2]);

            list.add(Json.obj(
                    "ticketNumber", "TKT-" + t[0],
                    "busNumber", bus == null ? "" : bus[1],
                    "startLocation", route == null ? "" : route[1],
                    "destination", route == null ? "" : route[2],
                    "seatNumber", seat == null ? "" : seat[2],
                    "departure", schedule == null ? "" : schedule[3],
                    "fare", t[3],
                    "issueDate", t[4]
            ));
        }

        return Json.obj(
                "success", true,
                "tickets", list
        );
    }

    // 8. REPORT LOST ITEMS

    private Map<String, Object> reportLostItem(
            Map<String, Object> user,
            Map<String, String> data) throws IOException {

        String description = value(data, "description");
        String travelDate = value(data, "travelDate");

        if (description.isBlank() || travelDate.isBlank()) {
            return result(false,
                    "Please enter description and travel date.");
        }

        String id = String.valueOf(
                SharedData.nextId("lostitems.txt", "|"));

        SharedData.append(
                "lostitems.txt",
                id,
                String.valueOf(user.get("id")),
                description,
                travelDate,
                LocalDateTime.now().toString(),
                "REPORTED"
        );

        return result(true,
                "Lost item report submitted successfully.");
    }

    // 9. GIVE RATINGS

    private Map<String, Object> giveRating(
            Map<String, Object> user,
            Map<String, String> data) throws IOException {

        String busNumber = value(data, "busNumber");
        String rating = value(data, "rating");
        String comment = value(data, "comment");

        int score;

        try {
            score = Integer.parseInt(rating);
        } catch (Exception e) {
            return result(false, "Select a rating from 1 to 5.");
        }

        if (score < 1 || score > 5 || busNumber.isBlank()) {
            return result(false, "Please enter a valid bus and rating.");
        }

        String[] bus = null;

        for (String line : SharedData.readLines("buses.txt")) {
            String[] b = SharedData.pipe(line);

            if (b.length >= 2 &&
                    b[1].equalsIgnoreCase(busNumber)) {
                bus = b;
                break;
            }
        }

        if (bus == null) {
            return result(false, "Bus not found.");
        }

        SharedData.append(
                "ratings.txt",
                String.valueOf(user.get("id")),
                bus[0],
                String.valueOf(score),
                comment
        );

        return result(true,
                "Thank you! Your rating has been submitted.");
    }

    // 10. VIEW FEEDBACK

    private Map<String, Object> feedback(
            Map<String, String> data) throws IOException {

        String busNumber = value(data, "busNumber");
        String busId = "";

        for (String line : SharedData.readLines("buses.txt")) {
            String[] b = SharedData.pipe(line);

            if (b.length >= 2 &&
                    b[1].equalsIgnoreCase(busNumber)) {
                busId = b[0];
                break;
            }
        }

        List<Map<String, Object>> list = new ArrayList<>();

        if (busId.isBlank()) {
            return Json.obj(
                    "success", true,
                    "feedback", list
            );
        }

        for (String line : SharedData.readLines("ratings.txt")) {

            String[] r = SharedData.pipe(line);

            if (r.length < 4 || !r[1].equals(busId)) {
                continue;
            }

            String passengerName = "Passenger";

            for (String userLine : SharedData.readLines("users.txt")) {
                String[] u = SharedData.pipe(userLine);

                if (u.length >= 2 && u[0].equals(r[0])) {
                    passengerName = u[1];
                    break;
                }
            }

            list.add(Json.obj(
                    "passengerName", passengerName,
                    "rating", r[2],
                    "comment", r[3]
            ));
        }

        return Json.obj(
                "success", true,
                "feedback", list
        );
    }

    // 11. SUBMIT COMPLAINTS

    private Map<String, Object> submitComplaint(
            Map<String, Object> user,
            Map<String, String> data) throws IOException {

        String description = value(data, "description");

        if (description.isBlank()) {
            return result(false,
                    "Please enter your complaint.");
        }

        String id = String.valueOf(
                SharedData.nextId("complaints.txt", "|"));

        SharedData.append(
                "complaints.txt",
                id,
                String.valueOf(user.get("id")),
                description,
                LocalDateTime.now().toString()
        );

        return result(true,
                "Your complaint has been submitted.");
    }

    // COMMON HELPER METHODS

    private String[] find(String file, String id)
            throws IOException {

        for (String line : SharedData.readLines(file)) {

            if (line.isBlank()) continue;

            String[] p = SharedData.pipe(line);

            if (p.length > 0 && p[0].equals(id)) {
                return p;
            }
        }

        return null;
    }

    private Map<String, String> readBody(HttpExchange x)
            throws IOException {

        String body = new String(
                x.getRequestBody().readAllBytes(),
                StandardCharsets.UTF_8
        );

        return parseParams(body);
    }

    private Map<String, String> parseParams(String raw) {

        Map<String, String> map = new HashMap<>();

        if (raw == null || raw.isBlank()) {
            return map;
        }

        for (String pair : raw.split("&")) {

            String[] p = pair.split("=", 2);

            String key = decode(p[0]);
            String value = p.length > 1
                    ? decode(p[1]) : "";

            map.put(key, value);
        }

        return map;
    }

    private String decode(String value) {
        return URLDecoder.decode(
                value, StandardCharsets.UTF_8);
    }

    private String value(Map<String, String> data, String key) {
        return data.getOrDefault(key, "").trim();
    }

    private Map<String, Object> result(
            boolean success, String message) {

        return Json.obj(
                "success", success,
                "message", message
        );
    }

    private void send(
            HttpExchange x,
            int status,
            Map<String, Object> body) throws IOException {

        HttpSupport.json(x, status, body);
    }
}
