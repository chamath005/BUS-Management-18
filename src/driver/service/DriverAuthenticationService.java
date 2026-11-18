package driver.service;
import common.SharedData;
import driver.model.*;
import java.io.IOException;
import java.util.*;
public class DriverAuthenticationService {
 public Driver authenticate(String username,String password)throws IOException{
  for(String line:SharedData.readLines("drivers.txt")){
   String[] p=line.split(",",-1); if(p.length<6)continue;
   if((p[1].equalsIgnoreCase(username.trim())||p[2].equalsIgnoreCase(username.trim()))&&p[3].equals(password)){
    Driver d=new Driver(Integer.parseInt(p[0]),p[1],p[2],p[3],p[4],p[5]);
    Integer tripId=null; for(String a:SharedData.readLines("driver_assignments.txt")){String[] x=SharedData.pipe(a);if(x.length>=2&&x[0].equals(p[0])){tripId=Integer.parseInt(x[1]);break;}}
    if(tripId!=null)d.setAssignedTrip(loadTrip(tripId)); return d;
   }
  }return null;
 }
 public static Trip loadTrip(int id)throws IOException{
  for(String line:SharedData.readLines("trips.txt")){String[]p=SharedData.pipe(line);if(p.length<5||!p[0].equals(String.valueOf(id)))continue;
   Bus b=loadBus(Integer.parseInt(p[1]));Route r=loadRoute(Integer.parseInt(p[2]));if(b==null||r==null)return null;
   Trip t=new Trip(id,b,r,p[3],p[4]);if(p.length>5)t.setCurrentLocation(p[5]);return t;
  }return null;
 }
 public static Bus loadBus(int id)throws IOException{for(String line:SharedData.readLines("buses.txt")){String[]p=SharedData.pipe(line);if(p.length>=5&&p[0].equals(String.valueOf(id)))return new Bus(id,p[1],num(p[2]),p[3],dbl(p[4]),p.length>5?p[5]:"Unknown");}return null;}
 public static Route loadRoute(int id)throws IOException{for(String line:SharedData.readLines("routes.txt")){String[]p=SharedData.pipe(line);if(p.length>=4&&p[0].equals(String.valueOf(id)))return new Route(id,p[1],p[2],dbl(p[3]));}return null;}
 private static int num(String s){try{return Integer.parseInt(s);}catch(Exception e){return 0;}} private static double dbl(String s){try{return Double.parseDouble(s);}catch(Exception e){return 0;}}
}
