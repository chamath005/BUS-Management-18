package owner.filehandling;
import owner.model.Ticket;
import common.SharedData;
import java.io.IOException;
public class TicketFile {
 public void saveTicket(Ticket t){try{SharedData.append("tickets.txt",String.valueOf(t.getTicketId()),String.valueOf(t.getPassengerId()),String.valueOf(t.getTrip().getTripId()),String.valueOf(t.getFare()),t.getTicketTime());System.out.println("Ticket saved successfully!");}catch(IOException e){System.out.println("Could not save ticket: "+e.getMessage());}}
 public void readTickets(){try{for(String r:SharedData.readLines("tickets.txt"))System.out.println(r);}catch(IOException e){System.out.println("Could not read tickets: "+e.getMessage());}}
 public void viewPassengersInBus(int busId){try{System.out.println("\n===== PASSENGERS IN BUS =====");boolean found=false;for(String row:SharedData.readLines("tickets.txt")){String[]p=SharedData.pipe(row);if(p.length<5)continue;owner.model.Trip trip=new TripFile().findTrip(Integer.parseInt(p[2]));if(trip!=null&&trip.getBus().getBusId()==busId){String name=new UserFile().findPassengerName(Integer.parseInt(p[1]));System.out.println("Passenger ID: "+p[1]+" | Name: "+name+" | Ticket ID: "+p[0]+" | Trip ID: "+p[2]);found=true;}}if(!found)System.out.println("No passengers found in this bus!");}catch(Exception e){System.out.println("Could not display passengers: "+e.getMessage());}}
}
