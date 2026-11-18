import java.util.List;

public class passengerreg {

    public boolean registerPassenger(String name, String email,
                                     String password, String phone) {

        List<String[]> passengers = FileHandling.read("passengers.txt");

        for (String[] passenger : passengers) {
            if (passenger.length >= 5 && passenger[2].equalsIgnoreCase(email)) {
                return false;
            }
        }

        int passengerId = FileHandling.nextId("passengers.txt");

        FileHandling.append("passengers.txt",
                passengerId + "|" + name + "|" + email + "|" + password + "|" + phone);

        return true;
    }
}
