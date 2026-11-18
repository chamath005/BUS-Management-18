
package owner.service;
import owner.model.*;

import owner.filehandling.UserFile;

import java.util.Scanner;

public class AuthService {
    private UserFile userFile = new UserFile();
    public String login(String email, String password) {
        String role = userFile.checkLogin(email, password);
        if (role != null) {
            System.out.println("Login successful!");
            return role;
        } else {
            System.out.println("Invalid email or password!");
            return null;}
    }
    public void  register(User user,String password){
        userFile.saveUser(user,password);
        System.out.println("Registration successful");

    }private Scanner input=new Scanner(System.in);
    public void registerUser(){
        System.out.println("enter User id:");
        int userId=input.nextInt();
        input.nextLine();

        System.out.println("Enter Name");
        String name=input.nextLine();

        System.out.println("Enter Email");
        String email=input.nextLine();

        System.out.println("enter password");
        String password=input.nextLine();

        System.out.println("Select role");
        System.out.println("1.Bus owner");
        System.out.println("2.Driver");
        System.out.println("3.Conductor");
        System.out.println("4.passenger");

        int choice=input.nextInt();

        User user=null;

        switch(choice) {
            case 1:
                user = new BusOwner(userId, name, email, password);
                break;

            case 2:
                user = new Driver(userId, name, email, password);
                break;

            case 3:
                user = new Conductor(userId, name, email, password);
                break;
            case 4:
                user = new Passenger(userId, name, email, password);
                break;
            default:
                System.out.println("Invalid role selection");
                return;
        }
        register(user, password);
    }
}

