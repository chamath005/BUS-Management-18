package conductor.filehandling;

import conductor.model.Conductor;
import conductor.model.Ticket;
import conductor.model.LostItemReport;
import conductor.service.ConductorService;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Properties;
import common.SharedData;

public class ConductorFileHandler {

    private final Path file;

    public ConductorFileHandler(String fileName) {
        file = SharedData.getDataDirectory().resolve(fileName);
    }


    public void save(ConductorService service) throws IOException {
        Properties data = new Properties();
        Conductor conductor = service.getConductor();

        data.setProperty("version", "1");
        data.setProperty("userId", String.valueOf(conductor.getUserId()));
        data.setProperty("capacity", String.valueOf(conductor.getSeatCapacity()));

        List<String> passengers = conductor.getOnboardPassengerIds();

        data.setProperty("passengerCount", String.valueOf(passengers.size()));

        for (int i = 0; i < passengers.size(); i++) {
            data.setProperty("passenger." + i, passengers.get(i));
        }

        List<Ticket> tickets = service.getTickets();

        data.setProperty("ticketCount", String.valueOf(tickets.size()));

        for (int i = 0; i < tickets.size(); i++) {
            Ticket ticket = tickets.get(i);
            String key = "ticket." + i + ".";

            data.setProperty(key + "passenger", ticket.getPassengerId());
            data.setProperty(key + "fare", String.valueOf(ticket.getFare()));
            data.setProperty(key + "active", String.valueOf(ticket.isActive()));
        }

        List<LostItemReport> reports = service.getLostItemReports();

        data.setProperty("reportCount", String.valueOf(reports.size()));

        for (int i = 0; i < reports.size(); i++) {
            LostItemReport report = reports.get(i);
            String key = "report." + i + ".";

            data.setProperty(key + "passenger", report.getPassengerId());
            data.setProperty(key + "description", report.getDescription());
            data.setProperty(key + "status", report.getStatus());
        }

        Files.createDirectories(file.getParent());

        Path temporary = Files.createTempFile(
                file.getParent(), "conductor-", ".tmp"
        );

        try {
            try (Writer writer = Files.newBufferedWriter(
                    temporary, StandardCharsets.UTF_8)) {

                data.store(writer, "Conductor records");
            }

            Files.move(
                    temporary,
                    file,
                    StandardCopyOption.REPLACE_EXISTING
            );

        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private String value(Properties data, String key) {
        String result = data.getProperty(key);

        if (result == null) {
            throw new IllegalArgumentException("Missing field: " + key);
        }

        return result;
    }

    private int count(Properties data, String key) {
        int result = Integer.parseInt(value(data, key));

        if (result < 0 || result > 100000) {
            throw new IllegalArgumentException(
                    "Invalid record count: " + key
            );
        }

        return result;
    }

    public ConductorService load(Conductor profile) throws IOException {
        if (!Files.exists(file)) {
            return new ConductorService(profile);
        }

        Properties data = new Properties();

        try {
            try (Reader reader = Files.newBufferedReader(
                    file, StandardCharsets.UTF_8)) {

                data.load(reader);
            }

            if (!value(data, "version").equals("1")
                    || Integer.parseInt(value(data, "userId"))
                    != profile.getUserId()) {

                throw new IllegalArgumentException(
                        "Wrong file version or conductor."
                );
            }

            Conductor conductor = new Conductor(
                    profile.getUserId(),
                    profile.getName(),
                    profile.getEmail(),
                    profile.getPassword(),
                    profile.getPhone(),
                    profile.getEmployeeId(),
                    Integer.parseInt(value(data, "capacity"))
            );

            ConductorService service = new ConductorService(conductor);

            int passengers = count(data, "passengerCount");

            for (int i = 0; i < passengers; i++) {
                service.recordBoarding(value(data, "passenger." + i));
            }

            int tickets = count(data, "ticketCount");

            for (int i = 0; i < tickets; i++) {
                String key = "ticket." + i + ".";
                String active = value(data, key + "active");

                if (!active.equals("true") && !active.equals("false")) {
                    throw new IllegalArgumentException(
                            "Invalid ticket state."
                    );
                }

                service.restoreTicket(
                        value(data, key + "passenger"),
                        Integer.parseInt(value(data, key + "fare")),
                        Boolean.parseBoolean(active)
                );
            }

            int reports = count(data, "reportCount");

            for (int i = 0; i < reports; i++) {
                String key = "report." + i + ".";

                LostItemReport report = service.reportLostItem(
                        value(data, key + "passenger"),
                        value(data, key + "description")
                );

                String status = value(data, key + "status");

                if (status.equals("FOUND") || status.equals("RETURNED")) {
                    service.updateLostItemStatus(
                            report.getReportId(), "FOUND"
                    );

                    if (status.equals("RETURNED")) {
                        service.updateLostItemStatus(
                                report.getReportId(), "RETURNED"
                        );
                    }

                } else if (!status.equals("REPORTED")) {
                    throw new IllegalArgumentException(
                            "Invalid report status."
                    );
                }
            }

            return service;

        } catch (IllegalArgumentException e) {
            throw new IOException(
                    "Saved data is invalid: " + e.getMessage(), e
            );
        }
    }
}