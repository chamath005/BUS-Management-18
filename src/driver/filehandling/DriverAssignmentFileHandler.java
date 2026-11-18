package driver.filehandling;
import common.SharedData;import java.io.IOException;
public class DriverAssignmentFileHandler {
 public Integer findTripId(int driverId){try{for(String row:SharedData.readLines("driver_assignments.txt")){String[]p=SharedData.pipe(row);if(p.length>=2&&p[0].equals(String.valueOf(driverId)))return Integer.parseInt(p[1]);}}catch(IOException|NumberFormatException e){System.out.println("Could not read driver assignments: "+e.getMessage());}return null;}
}
