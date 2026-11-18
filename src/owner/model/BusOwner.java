package owner.model;

public class BusOwner extends User{
    public BusOwner (int userId,String name ,String email,String password){
        super(userId, name, email, password);
    }
    @Override
    public void displayDashboard(){
        System.out.println("Welcome to Bus Owner Dashboard");}

    @Override
    public String getRole(){
        return "owner";
    }
    }

