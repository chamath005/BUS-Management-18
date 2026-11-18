package conductor;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import common.*;
import conductor.filehandling.ConductorFileHandler;
import conductor.model.Conductor;
import conductor.model.LostItemReport;
import conductor.model.Ticket;
import conductor.service.ConductorService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ConductorHandler {
    private final Map<Integer, ConductorService> services = new ConcurrentHashMap<>();

    public static void register(HttpServer server) {
        ConductorHandler handler = new ConductorHandler();
        server.createContext("/api/conductor/", handler::handle);
    }

    private synchronized ConductorService getServiceFor(Map<String, Object> user) throws IOException {
        int userId = Integer.parseInt(String.valueOf(user.get("id")));
        ConductorService existing = services.get(userId);
        if (existing != null) return existing;

        String name = String.valueOf(user.get("name"));
        String email = String.valueOf(user.get("email"));
        String phone = String.valueOf(user.getOrDefault("phone", "0770000000"));
        String empId = String.valueOf(user.getOrDefault("extra", "CON" + userId));
        if (empId.isBlank()) empId = "CON" + userId;

        Conductor profile = new Conductor(userId, name, email, "", phone, empId, 40);
        ConductorFileHandler files = new ConductorFileHandler("conductor_" + userId + ".properties");
        ConductorService loaded = files.load(profile);
        files.save(loaded);
        services.put(userId, loaded);
        return loaded;
    }

    private void handle(HttpExchange exchange) throws IOException {
        try {
            Map<String, Object> user = UnifiedAuth.requireRole(exchange, "conductor");
            if (user == null) return;

            int userId = Integer.parseInt(String.valueOf(user.get("id")));
            ConductorService service = getServiceFor(user);
            ConductorFileHandler files = new ConductorFileHandler("conductor_" + userId + ".properties");

            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod();
            Map<String, String> input = method.equals("POST") ? HttpSupport.form(exchange) : new HashMap<>();

            if (path.equals("/api/conductor/state") && method.equals("GET")) {
                sendRawJson(exchange, 200, state(service));
                return;
            }
            if (path.equals("/api/conductor/action") && method.equals("POST")) {
                action(exchange, service, files, input);
                return;
            }
            sendRawJson(exchange, 404, "{\"error\":\"Not found.\"}");
        } catch (NumberFormatException e) {
            sendRawJson(exchange, 400, "{\"error\":\"Enter a valid number. Fares must be whole rupees.\"}");
        } catch (IllegalArgumentException e) {
            sendRawJson(exchange, 400, "{\"error\":" + quote(e.getMessage()) + "}");
        } catch (Exception e) {
            e.printStackTrace();
            sendRawJson(exchange, 500, "{\"error\":\"File operation failed.\"}");
        }
    }

    private void action(HttpExchange exchange, ConductorService service, ConductorFileHandler files, Map<String, String> input) throws IOException {
        String action = HttpSupport.required(input, "action");
        if (action.equals("commission")) {
            double amount = service.calculateCommission(Double.parseDouble(HttpSupport.required(input, "rate")));
            sendRawJson(exchange, 200, "{\"amount\":" + amount + "}");
            return;
        }
        String message;
        switch (action) {
            case "board" -> {
                service.recordBoarding(HttpSupport.required(input, "passengerId"));
                message = "Passenger boarded.";
            }
            case "exit" -> {
                service.recordExiting(HttpSupport.required(input, "passengerId"));
                message = "Passenger exit recorded.";
            }
            case "ticket" -> {
                String pid = HttpSupport.required(input, "passengerId");
                int fare = Integer.parseInt(HttpSupport.required(input, "fare"));
                Ticket ticket = service.issueTicket(pid, fare);
                int sharedTicketId = SharedData.nextId("tickets.txt", "|");
                SharedData.append("tickets.txt", String.valueOf(sharedTicketId), pid, "1", String.valueOf(fare), LocalTime.now().withNano(0).toString());
                message = "Ticket " + ticket.getTicketId() + " issued.";
            }
            case "lost" -> {
                String pid = HttpSupport.required(input, "passengerId");
                String desc = HttpSupport.required(input, "description");
                LostItemReport report = service.reportLostItem(pid, desc);
                int sharedLostId = SharedData.nextId("lostitems.txt", "|");
                SharedData.append("lostitems.txt", String.valueOf(sharedLostId), pid, desc, LocalDate.now().toString(), LocalDateTime.now().toString());
                message = "Report " + report.getReportId() + " created.";
            }
            case "status" -> {
                service.updateLostItemStatus(Integer.parseInt(HttpSupport.required(input, "reportId")), HttpSupport.required(input, "status"));
                message = "Report status updated.";
            }
            case "save" -> message = "Records saved.";
            default -> throw new IllegalArgumentException("Unknown action.");
        }
        files.save(service);
        sendRawJson(exchange, 200, "{\"message\":" + quote(message) + "}");
    }

    private String state(ConductorService service) {
        Conductor profile = service.getConductor();
        List<String> passengers = new ArrayList<>();
        List<String> without = service.getPassengersWithoutTickets();
        for (String id : profile.getOnboardPassengerIds()) {
            passengers.add("{\"id\":" + quote(id) + ",\"hasTicket\":" + !without.contains(id) + "}");
        }
        List<String> tickets = new ArrayList<>();
        for (Ticket ticket : service.getTickets()) {
            tickets.add("{\"id\":" + ticket.getTicketId() + ",\"passengerId\":"
                    + quote(ticket.getPassengerId()) + ",\"fare\":" + ticket.getFare()
                    + ",\"active\":" + ticket.isActive() + "}");
        }
        List<String> reports = new ArrayList<>();
        for (LostItemReport report : service.getLostItemReports()) {
            reports.add("{\"id\":" + report.getReportId() + ",\"passengerId\":"
                    + quote(report.getPassengerId()) + ",\"description\":" + quote(report.getDescription())
                    + ",\"status\":" + quote(report.getStatus()) + "}");
        }
        return "{\"name\":" + quote(profile.getName())
                + ",\"empId\":" + quote(profile.getEmployeeId())
                + ",\"count\":" + service.getPassengerCount()
                + ",\"seats\":" + service.getAvailableSeats()
                + ",\"capacity\":" + profile.getSeatCapacity()
                + ",\"revenue\":" + service.getTotalRevenue()
                + ",\"ticketCount\":" + service.getTicketCount()
                + ",\"unticketed\":" + without.size()
                + ",\"passengers\":[" + String.join(",", passengers)
                + "],\"tickets\":[" + String.join(",", tickets)
                + "],\"reports\":[" + String.join(",", reports) + "]}";
    }

    private String quote(String value) {
        if (value == null) return "\"\"";
        StringBuilder result = new StringBuilder("\"");
        for (char c : value.toCharArray()) {
            if (c == '"' || c == '\\') result.append('\\').append(c);
            else if (c < 32) result.append(String.format("\\u%04x", (int) c));
            else result.append(c);
        }
        return result.append('"').toString();
    }

    private void sendRawJson(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        exchange.sendResponseHeaders(status, bytes.length);
        try (var os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
