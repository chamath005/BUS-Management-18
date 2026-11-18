package owner.model;

public class Passenger extends User {
    public Passenger(int userId,String name,String email,String password){
        super(userId,name, email,password);}

        @Override
        public void displayDashboard(){
            System.out.println("Welcome to Passenger Dashboard");}

    @Override
    public String getRole(){
        return "Passenger";
    }

}
