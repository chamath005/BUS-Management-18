import java.util.List;

public class viewschedules {

    public static void viewSchedules() {
        List<String[]> buses = FileHandling.read("buses.txt");
        List<String[]> routes = FileHandling.read("routes.txt");
        List<String[]> schedules = FileHandling.read("schedules.txt");
        boolean found = false;

        System.out.println("\n========== BUS SCHEDULES ==========");

        for (String[] s : schedules) {
            if (s.length < 7) continue;
            String[] bus = searchbuses.findById(buses, s[1]);
            String[] route = searchbuses.findById(routes, s[2]);
            if (bus == null || route == null) continue;

            found = true;
            searchbuses.printBus(bus, route, s);
            System.out.println("Operating Days: " + s[6]);
            System.out.println("--------------------------------");
        }

        if (!found) System.out.println("No bus schedules available.");
    }
}
