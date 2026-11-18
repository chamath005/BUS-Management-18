package driver.filehandling;
import common.SharedData;import driver.model.Driver;import java.io.IOException;import java.util.*;
public class DriverFileHandler {
 public List<Driver> loadDrivers(){List<Driver>out=new ArrayList<>();try{for(String line:SharedData.readLines("drivers.txt")){String[]p=line.split(",",-1);if(p.length>=6)try{out.add(new Driver(Integer.parseInt(p[0]),p[1],p[2],p[3],p[4],p[5]));}catch(NumberFormatException ignored){}}}catch(IOException e){System.out.println("Could not read drivers: "+e.getMessage());}return out;}
 public Driver authenticate(String username,String password){for(Driver d:loadDrivers())if((d.getEmail().equalsIgnoreCase(username.trim())||d.getName().equalsIgnoreCase(username.trim()))&&d.login(password))return d;return null;}
 public void saveDrivers(List<Driver> drivers){try{List<String>rows=new ArrayList<>();for(Driver d:drivers)rows.add(String.join(",",String.valueOf(d.getUserId()),SharedData.clean(d.getName()),SharedData.clean(d.getEmail()),SharedData.clean(d.getPassword()),SharedData.clean(d.getPhone()),SharedData.clean(d.getLicenseNumber())));SharedData.replaceLines("drivers.txt",rows);}catch(IOException e){System.out.println("Could not save drivers: "+e.getMessage());}}
}
