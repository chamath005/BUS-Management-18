package owner.filehandling;
import owner.model.User;
import common.SharedData;
import java.io.IOException;
public class UserFile {
 public void saveUser(User u,String password){try{SharedData.append("users.txt",String.valueOf(u.getUserId()),u.getName(),u.getEmail(),password,u.getRole());}catch(IOException e){System.out.println("Could not save user: "+e.getMessage());}}
 public void readUsers(){try{for(String r:SharedData.readLines("users.txt"))System.out.println(r);}catch(IOException e){System.out.println("Could not read users: "+e.getMessage());}}
 public String checkLogin(String email,String password){try{for(String r:SharedData.readLines("users.txt")){String[]p=SharedData.pipe(r);if(p.length>=5&&p[2].equalsIgnoreCase(email)&&p[3].equals(password))return p[4];}}catch(IOException e){System.out.println("Could not read users: "+e.getMessage());}return null;}
 public String findPassengerName(int id){try{for(String r:SharedData.readLines("users.txt")){String[]p=SharedData.pipe(r);if(p.length>=5&&p[0].equals(String.valueOf(id))&&p[4].equalsIgnoreCase("passenger"))return p[1];}}catch(IOException e){System.out.println("Could not find passenger: "+e.getMessage());}return null;
 }
}
