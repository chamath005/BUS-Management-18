package driver.filehandling;
import common.SharedData;import driver.model.*;import java.io.IOException;import java.time.LocalDateTime;
public class DriverMessageFileHandler {
 public void saveBreakdown(BreakdownReport r){append("breakdown_reports.txt",r.getReportId(),String.valueOf(r.getDriverId()),r.getDriverName(),r.getBusNumber(),r.getCategory(),r.getDescription(),r.getSeverity(),r.getReportDate());}
 public void saveAccident(AccidentReport r){append("accident_reports.txt",r.getReportId(),String.valueOf(r.getDriverId()),r.getDriverName(),r.getBusNumber(),r.getLocation(),r.getDescription(),r.getSeverity(),r.getReportDate());}
 public void saveReplacementRequest(ReplacementRequest r){append("replacement_requests.txt",r.getRequestId(),String.valueOf(r.getDriverId()),r.getDriverName(),r.getBusNumber(),r.getReason(),r.getRequestDate(),r.getStatus());}
 public void saveTripUpdate(Driver d,Trip t){String bus=t.getBus()==null?"N/A":t.getBus().getRegistrationNumber(),now=LocalDateTime.now().toString();append("trip_updates.txt",String.valueOf(d.getUserId()),d.getName(),String.valueOf(t.getTripId()),bus,t.getStatus(),t.getCurrentLocation(),now);append("location_updates.txt",String.valueOf(d.getUserId()),d.getName(),String.valueOf(t.getTripId()),bus,t.getCurrentLocation(),now);}
 private void append(String file,String...fields){try{SharedData.append(file,fields);}catch(IOException e){System.out.println("Could not save shared record: "+e.getMessage());}}
}
