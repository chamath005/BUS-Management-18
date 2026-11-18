package owner.filehandling;
import owner.model.*;
import common.SharedData;
import java.io.IOException;
import java.util.*;
public class TripFile {
 public void saveTrip(Trip t){try{if(findTrip(t.getTripId())!=null)throw new IllegalArgumentException("Trip ID already exists.");SharedData.append("trips.txt",String.valueOf(t.getTripId()),String.valueOf(t.getBus().getBusId()),String.valueOf(t.getRoute().getRouteId()),t.getDepartureTime(),t.getStatus());System.out.println("Trip saved successfully!");}catch(Exception e){System.out.println("Could not save trip: "+e.getMessage());}}
 public void readTrips(){try{for(String r:SharedData.readLines("trips.txt"))System.out.println(r);}catch(IOException e){System.out.println("Could not read trips: "+e.getMessage());}}
 public Trip findTrip(int id){try{for(String row:SharedData.readLines("trips.txt")){String[]p=SharedData.pipe(row);if(p.length>=5&&p[0].equals(String.valueOf(id))){Bus b=new BusFile().findBus(Integer.parseInt(p[1]));Route r=new RouteFile().findRoute(Integer.parseInt(p[2]));if(b!=null&&r!=null){Trip t=new Trip(id,b,r,p[3],p[4]);return t;}}}}catch(Exception e){System.out.println("Could not find trip: "+e.getMessage());}return null;}
}
