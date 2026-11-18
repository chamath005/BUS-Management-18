package owner.filehandling;
import owner.model.Bus;
import common.SharedData;
import java.io.IOException;
import java.util.*;
public class BusFile {
 public void saveBus(Bus b){try{if(findBus(b.getBusId())!=null)throw new IllegalArgumentException("Bus ID already exists.");SharedData.append("buses.txt",String.valueOf(b.getBusId()),b.getRegistrationNumber(),String.valueOf(b.getCapacity()),b.getStatus(),String.valueOf(b.getSpeed()),"");System.out.println("Bus saved successfully!");}catch(Exception e){System.out.println("Could not save bus: "+e.getMessage());}}
 public void readBuses(){try{for(String r:SharedData.readLines("buses.txt"))System.out.println(r);}catch(IOException e){System.out.println("Could not read buses: "+e.getMessage());}}
 public void updateBus(int id,String status,double speed){try{List<String>rows=SharedData.readLines("buses.txt");boolean found=false;for(int i=0;i<rows.size();i++){String[]p=SharedData.pipe(rows.get(i));if(p.length>=5&&p[0].equals(String.valueOf(id))){p[3]=status;p[4]=String.valueOf(speed);rows.set(i,String.join("|",p));found=true;break;}}if(!found)throw new IllegalArgumentException("Bus not found.");SharedData.replaceLines("buses.txt",rows);System.out.println("Bus updated successfully!");}catch(Exception e){System.out.println("Could not update bus: "+e.getMessage());}}
 public void removeBus(int id){try{List<String>rows=SharedData.readLines("buses.txt");boolean removed=rows.removeIf(r->{String[]p=SharedData.pipe(r);return p.length>0&&p[0].equals(String.valueOf(id));});if(!removed)throw new IllegalArgumentException("Bus not found.");SharedData.replaceLines("buses.txt",rows);System.out.println("Bus removed successfully!");}catch(Exception e){System.out.println("Could not remove bus: "+e.getMessage());}}
 public Bus findBus(int id){try{for(String r:SharedData.readLines("buses.txt")){String[]p=SharedData.pipe(r);if(p.length>=5&&p[0].equals(String.valueOf(id)))return new Bus(id,p[1],Integer.parseInt(p[2]),p[3],Double.parseDouble(p[4]));}}catch(Exception e){System.out.println("Could not find bus: "+e.getMessage());}return null;}
}
