package owner.filehandling;
import owner.model.Route;
import common.SharedData;
import java.io.IOException;
import java.util.*;
public class RouteFile {
 public void saveRoute(Route r){try{if(findRoute(r.getRouteId())!=null)throw new IllegalArgumentException("Route ID already exists.");SharedData.append("routes.txt",String.valueOf(r.getRouteId()),r.getStartLocation(),r.getEndLocation(),String.valueOf(r.getDistance()));System.out.println("Route saved successfully!");}catch(Exception e){System.out.println("Could not save route: "+e.getMessage());}}
 public void readRoutes(){try{for(String r:SharedData.readLines("routes.txt"))System.out.println(r);}catch(IOException e){System.out.println("Could not read routes: "+e.getMessage());}}
 public void updateRoute(int id,String start,String end,double distance){try{List<String>rows=SharedData.readLines("routes.txt");boolean found=false;for(int i=0;i<rows.size();i++){String[]p=SharedData.pipe(rows.get(i));if(p.length>=4&&p[0].equals(String.valueOf(id))){rows.set(i,String.join("|",p[0],SharedData.clean(start),SharedData.clean(end),String.valueOf(distance)));found=true;break;}}if(!found)throw new IllegalArgumentException("Route not found.");SharedData.replaceLines("routes.txt",rows);System.out.println("Route updated successfully!");}catch(Exception e){System.out.println("Could not update route: "+e.getMessage());}}
 public void removeRoute(int id){try{List<String>rows=SharedData.readLines("routes.txt");boolean removed=rows.removeIf(r->{String[]p=SharedData.pipe(r);return p.length>0&&p[0].equals(String.valueOf(id));});if(!removed)throw new IllegalArgumentException("Route not found.");SharedData.replaceLines("routes.txt",rows);System.out.println("Route removed successfully!");}catch(Exception e){System.out.println("Could not remove route: "+e.getMessage());}}
 public Route findRoute(int id){try{for(String r:SharedData.readLines("routes.txt")){String[]p=SharedData.pipe(r);if(p.length>=4&&p[0].equals(String.valueOf(id)))return new Route(id,p[1],p[2],Double.parseDouble(p[3]));}}catch(Exception e){System.out.println("Could not find route: "+e.getMessage());}return null;}
}
