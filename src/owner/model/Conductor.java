package owner.model;

public class Conductor extends User {
    public Conductor(int userId,String name,String email,String password){
        super( userId, name, email, password);
    }
    @Override
    public void displayDashboard(){
        System.out.println("Welcome to Bus Conduuctor Dashboard");}

    @Override
    public String getRole(){
        return "Conductor";
    }
}
