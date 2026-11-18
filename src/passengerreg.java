import java.util.List;

public class passengerreg {

    public boolean registerPassenger(String name, String email,
                                     String password, String phone) {

        List<String[]> users = FileHandling.read("users.txt");

        for (String[] user : users) {
            if (user.length >= 5 && user[2].equalsIgnoreCase(email)) {
                return false;
            }
        }

        int passengerId = FileHandling.nextId("users.txt");

        FileHandling.append("users.txt",
                passengerId + "|" + name + "|" + email + "|" + password + "|Passenger|" + phone);

        return true;
    }

    public String[] login(String email, String password) {
        for (String[] user : FileHandling.read("users.txt")) {
            if (user.length >= 5 && user[2].equalsIgnoreCase(email) && user[3].equals(password)
                    && user[4].equalsIgnoreCase("Passenger")) {
                return user;
            }
        }
        return null;
    }
}
