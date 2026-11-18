package conductor.model;

public abstract class User {

    // User class attributes
    private int userId;
    private String name;
    private String email;
    private String password;
    private String phone;


    // Constructor

    public User(int userId, String name, String email,
                String password, String phone) {

        this.userId = userId;
        this.name = name;
        this.email = email;
        this.password = password;
        this.phone = phone;

    }


    // Getters

    public int getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getPassword() {
        return password;
    }


    // Setters

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }


    // Login Method

    public boolean login(String password) {

        return this.password.equals(password);

    }


    // Logout Method

    public void logout() {

        System.out.println(name + " Logged Out Successfully!");

    }


    // Register Method

    public void register() {

        System.out.println("User Registered Successfully!");

    }


    // View Profile Method

    public void viewProfile() {

        System.out.println("User ID: " + userId);
        System.out.println("Name: " + name);
        System.out.println("Email: " + email);
        System.out.println("Phone: " + phone);

    }
    // Add Abstract
    public abstract void displayRole();

}
