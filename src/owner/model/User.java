package owner.model;

public abstract class User {
    private int userId;
    private String name;
    private String email;
    private String password;

    public User(int userId,String name,String email,String password){
        this.userId=userId;
        this.name=name;
        this.email=email;
        this.password=password;
    }
    public int getUserId(){
        return userId;
    }
    public String getName(){
        return name;
    }
    public String getEmail() {
        return email;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    public abstract void displayDashboard();

    public abstract String getRole();


    public void logout(){
        System.out.println("Successfully logged out!");
    }
}
