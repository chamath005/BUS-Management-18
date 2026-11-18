package owner.model;

public class Driver extends User {
    public Driver(int userId,String name,String email,String password){
        super(userId,name,email,password);
    }
    @Override
    public void displayDashboard(){
        System.out.println("Welcome to Bus Driver Dashboard");
    }
    @Override
    public String getRole(){
        return "Driver";
    }

}

