package common;

import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/** Central authentication & session registry shared across all 4 modules. */
public final class UnifiedAuth {
    private static final Map<String, Map<String, Object>> SESSIONS = new ConcurrentHashMap<>();

    private UnifiedAuth() {}

    private static String extractToken(HttpExchange x) {
        String token = x.getRequestHeaders().getFirst("X-Session-Token");
        if (token == null || token.isBlank()) {
            String auth = x.getRequestHeaders().getFirst("Authorization");
            if (auth != null && auth.startsWith("Bearer ")) {
                token = auth.substring(7).trim();
            }
        }
        return (token == null || token.isBlank()) ? null : token;
    }

    public static Map<String, Object> getSession(HttpExchange x) {
        String token = extractToken(x);
        if (token == null) return null;
        return SESSIONS.get(token);
    }

    public static Map<String, Object> requireRole(HttpExchange x, String expectedRole) throws IOException {
        Map<String, Object> user = getSession(x);
        if (user == null) {
            HttpSupport.json(x, 401, Json.obj("success", false, "message", "Please sign in first."));
            return null;
        }
        String role = String.valueOf(user.get("role"));
        if (!role.equalsIgnoreCase(expectedRole)) {
            HttpSupport.json(x, 403, Json.obj("success", false, "message", "Access denied for role: " + role));
            return null;
        }
        return user;
    }

    public static void handleLogin(HttpExchange x) throws IOException {
        Map<String, String> f = HttpSupport.form(x);
        String loginId = HttpSupport.required(f, "email").trim();
        String pass = HttpSupport.required(f, "password");

        Map<String, Object> matchedUser = null;

        // 1. Check unified users.txt
        for (String line : SharedData.readLines("users.txt")) {
            if (line.isBlank()) continue;
            String[] p = SharedData.pipe(line);
            if (p.length >= 5) {
                boolean idMatch = p[2].equalsIgnoreCase(loginId) || p[1].equalsIgnoreCase(loginId);
                if (idMatch && p[3].equals(pass)) {
                    String role = p[4].trim().toLowerCase();
                    matchedUser = Json.obj(
                            "id", p[0].trim(),
                            "name", p[1].trim(),
                            "email", p[2].trim(),
                            "role", role,
                            "phone", p.length > 5 ? p[5].trim() : "",
                            "extra", p.length > 6 ? p[6].trim() : ""
                    );
                    break;
                }
            }
        }

        // 2. Fallback check drivers.txt
        if (matchedUser == null) {
            for (String line : SharedData.readLines("drivers.txt")) {
                if (line.isBlank()) continue;
                String[] p = line.split(",", -1);
                if (p.length >= 6 && (p[2].equalsIgnoreCase(loginId) || p[1].equalsIgnoreCase(loginId)) && p[3].equals(pass)) {
                    matchedUser = Json.obj(
                            "id", p[0].trim(),
                            "name", p[1].trim(),
                            "email", p[2].trim(),
                            "role", "driver",
                            "phone", p[4].trim(),
                            "extra", p[5].trim()
                    );
                    break;
                }
            }
        }

        // 3. Fallback check passengers.txt
        if (matchedUser == null) {
            for (String line : SharedData.readLines("passengers.txt")) {
                if (line.isBlank()) continue;
                String[] p = SharedData.pipe(line);
                if (p.length >= 5 && (p[2].equalsIgnoreCase(loginId) || p[1].equalsIgnoreCase(loginId)) && p[3].equals(pass)) {
                    matchedUser = Json.obj(
                            "id", p[0].trim(),
                            "name", p[1].trim(),
                            "email", p[2].trim(),
                            "role", "passenger",
                            "phone", p[4].trim(),
                            "extra", ""
                    );
                    break;
                }
            }
        }

        if (matchedUser == null) {
            HttpSupport.json(x, 401, Json.obj("success", false, "message", "Invalid email/username or password."));
            return;
        }

        String role = String.valueOf(matchedUser.get("role"));
        String redirect = switch (role) {
            case "owner" -> "/owner.html";
            case "driver" -> "/driver.html";
            case "conductor" -> "/conductor.html";
            default -> "/passenger.html";
        };

        String token = UUID.randomUUID().toString();
        SESSIONS.put(token, matchedUser);
        HttpSupport.json(x, 200, Json.obj(
                "success", true,
                "token", token,
                "user", matchedUser,
                "redirect", redirect
        ));
    }

    public static Map<String, Object> createUserRecord(String name, String email, String pass, String role, String phone, String extra, String customId) throws IOException {
        String cleanRole = role == null ? "passenger" : role.trim().toLowerCase();
        if (!List.of("owner", "driver", "conductor", "passenger").contains(cleanRole)) {
            throw new IllegalArgumentException("Invalid role selected.");
        }
        String cleanName = SharedData.clean(name);
        String cleanEmail = SharedData.clean(email);
        String cleanPhone = SharedData.clean(phone);
        String cleanExtra = SharedData.clean(extra);
        if (cleanName.isBlank() || cleanEmail.isBlank() || pass == null || pass.isBlank()) {
            throw new IllegalArgumentException("Name, email, and password are required.");
        }

        int id = (customId == null || customId.isBlank())
                ? SharedData.nextId("users.txt", "|")
                : Integer.parseInt(customId.trim());

        for (String line : SharedData.readLines("users.txt")) {
            if (line.isBlank()) continue;
            String[] p = SharedData.pipe(line);
            if (p.length >= 5 && (p[2].equalsIgnoreCase(cleanEmail) || p[0].equals(String.valueOf(id)))) {
                throw new IllegalArgumentException("Email or User ID already exists.");
            }
        }

        if (cleanRole.equals("driver")) {
            int driverId = SharedData.nextId("drivers.txt", ",");
            if (cleanExtra.isEmpty()) cleanExtra = "LIC-" + driverId;
            SharedData.appendCsv("drivers.txt", String.valueOf(driverId), cleanName, cleanEmail, pass, cleanPhone, cleanExtra);
        } else if (cleanRole.equals("conductor")) {
            if (cleanExtra.isEmpty()) cleanExtra = "CON" + id;
        } else if (cleanRole.equals("passenger")) {
            SharedData.append("passengers.txt", String.valueOf(id), cleanName, cleanEmail, pass, cleanPhone);
        }

        SharedData.append("users.txt", String.valueOf(id), cleanName, cleanEmail, pass, cleanRole, cleanPhone, cleanExtra);

        return Json.obj(
                "id", String.valueOf(id),
                "name", cleanName,
                "email", cleanEmail,
                "role", cleanRole,
                "phone", cleanPhone,
                "extra", cleanExtra
        );
    }

    public static void handleRegister(HttpExchange x) throws IOException {
        Map<String, String> f = HttpSupport.form(x);
        String name = HttpSupport.required(f, "name");
        String email = HttpSupport.required(f, "email");
        String pass = HttpSupport.required(f, "password");
        String role = f.getOrDefault("role", "passenger").trim().toLowerCase();
        String phone = f.getOrDefault("phone", "").trim();
        String extra = f.getOrDefault("extra", "").trim();
        String customId = f.getOrDefault("userId", "").trim();

        Map<String, Object> created = createUserRecord(name, email, pass, role, phone, extra, customId);
        HttpSupport.json(x, 200, Json.obj(
                "success", true,
                "message", "Account created for " + created.get("name") + " (" + role + ", ID #" + created.get("id") + ")! Ready to sign in.",
                "user", created
        ));
    }

    public static void handleMe(HttpExchange x) throws IOException {
        Map<String, Object> user = getSession(x);
        if (user == null) {
            HttpSupport.json(x, 401, Json.obj("success", false, "message", "Not signed in."));
            return;
        }
        HttpSupport.json(x, 200, Json.obj("success", true, "user", user));
    }

    public static void handleLogout(HttpExchange x) throws IOException {
        String token = extractToken(x);
        if (token != null) SESSIONS.remove(token);
        HttpSupport.json(x, 200, Json.obj("success", true));
    }
}
