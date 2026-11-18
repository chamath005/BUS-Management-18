package driver.model;

public abstract class User {
    private int userId;
    private String name;
    private String email;
    private String password;
    private String phone;

    public User(int userId, String name, String email,
                String password, String phone) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.password = password;
        this.phone = phone;
    }

    public int getUserId() { return userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getPassword() { return password; }

    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }

    public boolean login(String password) {
        return this.password != null && this.password.equals(password);
    }

    public void logout() {
        System.out.println(name + " logged out successfully!");
    }

    public void viewProfile() {
        System.out.println("User ID: " + userId);
        System.out.println("Name: " + name);
        System.out.println("Email: " + email);
        System.out.println("Phone: " + phone);
    }

    public abstract void displayRole();
}
