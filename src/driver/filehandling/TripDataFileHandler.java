package driver.filehandling;
import common.SharedData;import driver.model.*;import driver.service.DriverAuthenticationService;import java.io.IOException;import java.util.*;
public class TripDataFileHandler {
 public Trip loadTrip(int tripId){try{return DriverAuthenticationService.loadTrip(tripId);}catch(IOException e){System.out.println("Could not load trip: "+e.getMessage());return null;}}
 public void updateTripStatus(int tripId,String status,String location){try{List<String>rows=SharedData.readLines("trips.txt");boolean found=false;for(int i=0;i<rows.size();i++){String[]p=SharedData.pipe(rows.get(i));if(p.length>=5&&p[0].equals(String.valueOf(tripId))){p=Arrays.copyOf(p,Math.max(6,p.length));p[4]=SharedData.clean(status);p[5]=SharedData.clean(location);rows.set(i,String.join("|",p));found=true;break;}}if(!found)throw new IllegalArgumentException("Trip not found.");SharedData.replaceLines("trips.txt",rows);}catch(Exception e){System.out.println("Could not update trip: "+e.getMessage());}}
}
