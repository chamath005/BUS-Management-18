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
    private static final int DEFAULT_PORT = 8080;

    private static Path resolveWebRoot() {
        Path direct = Paths.get("web").toAbsolutePath().normalize();
        if (Files.isDirectory(direct)) return direct;

        Path parent = Paths.get("../web").toAbsolutePath().normalize();
        if (Files.isDirectory(parent)) return parent;

        return direct;
    }

    private static HttpServer createServer() throws IOException {
        int preferredPort = Integer.getInteger(
                "busmanager.port", DEFAULT_PORT
        );

        IOException lastError = null;

        // If the default port is busy, try the next available ports.
        for (int offset = 0; offset < 20; offset++) {
            int candidatePort = preferredPort + offset;

            try {
                return HttpServer.create(
                        new InetSocketAddress(candidatePort), 0
                );
            } catch (java.net.BindException e) {
                lastError = e;
            }
        }

        throw new IOException(
                "Could not start BusManager. Ports "
                        + preferredPort + " to "
                        + (preferredPort + 19)
                        + " are already in use.",
                lastError
        );
    }

    public static void main(String[] args) throws Exception {
        SharedData.initialize();

        Path webRoot = resolveWebRoot();

        HttpServer server = createServer();
        int port = server.getAddress().getPort();

        // Unified authentication routes
        server.createContext(
                "/api/login",
                x -> wrap(x, () -> UnifiedAuth.handleLogin(x))
        );

        server.createContext(
                "/api/register",
                x -> wrap(x, () -> UnifiedAuth.handleRegister(x))
        );

        server.createContext(
                "/api/me",
                x -> wrap(x, () -> UnifiedAuth.handleMe(x))
        );

        server.createContext(
                "/api/logout",
                x -> wrap(x, () -> UnifiedAuth.handleLogout(x))
        );

        // Register available modules
        List<String> loadedModules = new ArrayList<>();

        String[][] modules = {
                {"Owner", "owner.OwnerHandler"},
                {"Driver", "driver.DriverHandler"},
                {"Conductor", "conductor.ConductorHandler"},
                {"Passenger", "passenger.PassengerHandler"}
        };

        for (String[] module : modules) {
            if (registerModuleIfPresent(server, module[1])) {
                loadedModules.add(module[0]);
            }
        }

        final List<String> activeModules = List.copyOf(loadedModules);

        server.createContext(
                "/api/modules",
                x -> wrap(x, () ->
                        HttpSupport.json(
                                x,
                                200,
                                Json.obj(
                                        "success", true,
                                        "modules", activeModules
                                )
                        )
                )
        );

        // Serve frontend files
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
        System.out.println(" Open Login Page : http://localhost:" + port);
        System.out.println(" Active Modules  : " + loadedModules);
        System.out.println(
                " Shared Data Dir : " + SharedData.getDataDirectory()
        );
        System.out.println("======================================================");

        try {
            if (System.getProperty("os.name", "")
                    .toLowerCase().contains("win")) {

                new ProcessBuilder(
                        "cmd",
                        "/c",
                        "start",
                        "",
                        "http://localhost:" + port
                ).start();
            }
        } catch (Exception ignored) {
        }
    }

    private static boolean registerModuleIfPresent(
            HttpServer server,
            String className
    ) {
        try {
            Class<?> clazz = Class.forName(className);

            Method registerMethod = clazz.getMethod(
                    "register",
                    HttpServer.class
            );

            registerMethod.invoke(null, server);

            return true;

        } catch (ClassNotFoundException e) {
            return false;

        } catch (Exception e) {
            System.err.println(
                    "Failed to initialize module "
                            + className + ": " + e.getMessage()
            );

            e.printStackTrace();

            return false;
        }
    }

    @FunctionalInterface
    private interface CheckedHandler {
        void run() throws Exception;
    }

    private static void wrap(
            HttpExchange x,
            CheckedHandler handler
    ) throws IOException {

        try {
            handler.run();

        } catch (IllegalArgumentException e) {
            HttpSupport.json(
                    x,
                    400,
                    Json.obj(
                            "success", false,
                            "message", e.getMessage()
                    )
            );

        } catch (Exception e) {
            e.printStackTrace();

            HttpSupport.json(
                    x,
                    500,
                    Json.obj(
                            "success", false,
                            "message", "Server error: " + e.getMessage()
                    )
            );
        }
    }
}