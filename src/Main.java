import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import common.*;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class Main {
    private static final int PORT = 8080;

    private static Path resolveWebRoot() {
        Path direct = Paths.get("web").toAbsolutePath().normalize();
        if (Files.isDirectory(direct)) return direct;
        Path parent = Paths.get("../web").toAbsolutePath().normalize();
        if (Files.isDirectory(parent)) return parent;
        return direct;
    }

    public static void main(String[] args) throws Exception {
        SharedData.initialize();
        Path webRoot = resolveWebRoot();

        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        // Unified Authentication Routes (Login, Register, Session Check, Logout)
        server.createContext("/api/login", x -> wrap(x, () -> UnifiedAuth.handleLogin(x)));
        server.createContext("/api/register", x -> wrap(x, () -> UnifiedAuth.handleRegister(x)));
        server.createContext("/api/me", x -> wrap(x, () -> UnifiedAuth.handleMe(x)));
        server.createContext("/api/logout", x -> wrap(x, () -> UnifiedAuth.handleLogout(x)));

        // Dynamically mount whichever sub-modules are present in the codebase
        List<String> loadedModules = new ArrayList<>();
        String[][] modules = {
                {"Owner", "owner.OwnerHandler"},
                {"Driver", "driver.DriverHandler"},
                {"Conductor", "conductor.ConductorHandler"},
                {"Passenger", "passenger.PassengerHandler"}
        };
        for (String[] m : modules) {
            if (registerModuleIfPresent(server, m[1])) {
                loadedModules.add(m[0]);
            }
        }

        final List<String> activeModules = List.copyOf(loadedModules);
        server.createContext("/api/modules", x -> wrap(x, () ->
                HttpSupport.json(x, 200, Json.obj("success", true, "modules", activeModules))
        ));

        // Static Web Server (Serves web/index.html as the ONE unified Login Page)
        server.createContext("/", x -> {
            try {
                HttpSupport.staticFile(x, webRoot);
            } catch (Exception e) {
                x.close();
            }
        });

        server.setExecutor(Executors.newCachedThreadPool());
        server.start();

        System.out.println("======================================================");
        System.out.println(" Bus Management System Started!");
        System.out.println(" Open Login Page : http://localhost:" + PORT);
        System.out.println(" Active Modules  : " + loadedModules);
        System.out.println(" Shared Data Dir : " + SharedData.getDataDirectory());
        System.out.println("======================================================");

        try {
            if (System.getProperty("os.name", "").toLowerCase().contains("win")) {
                new ProcessBuilder("cmd", "/c", "start", "", "http://localhost:" + PORT).start();
            }
        } catch (Exception ignored) {}
    }

    private static boolean registerModuleIfPresent(HttpServer server, String className) {
        try {
            Class<?> clazz = Class.forName(className);
            Method registerMethod = clazz.getMethod("register", HttpServer.class);
            registerMethod.invoke(null, server);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        } catch (Exception e) {
            System.err.println("Failed to initialize module " + className + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @FunctionalInterface
    private interface CheckedHandler {
        void run() throws Exception;
    }

    private static void wrap(HttpExchange x, CheckedHandler handler) throws IOException {
        try {
            handler.run();
        } catch (IllegalArgumentException e) {
            HttpSupport.json(x, 400, Json.obj("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            HttpSupport.json(x, 500, Json.obj("success", false, "message", "Server error: " + e.getMessage()));
        }
    }
}
