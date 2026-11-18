package common;
import com.sun.net.httpserver.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
public final class HttpSupport {
 private HttpSupport(){}
 public static Map<String,String> form(HttpExchange x)throws IOException{String body=new String(x.getRequestBody().readAllBytes(),StandardCharsets.UTF_8);Map<String,String> m=new HashMap<>();for(String part:body.split("&")){if(part.isEmpty())continue;String[] kv=part.split("=",2);m.put(URLDecoder.decode(kv[0],StandardCharsets.UTF_8),kv.length>1?URLDecoder.decode(kv[1],StandardCharsets.UTF_8):"");}return m;}
 public static String query(HttpExchange x,String key){Map<String,String> m=parseQuery(x.getRequestURI().getRawQuery());return m.getOrDefault(key,"");}
 private static Map<String,String> parseQuery(String q){Map<String,String>m=new HashMap<>();if(q==null)return m;for(String p:q.split("&")){String[]a=p.split("=",2);m.put(URLDecoder.decode(a[0],StandardCharsets.UTF_8),a.length>1?URLDecoder.decode(a[1],StandardCharsets.UTF_8):"");}return m;}
 public static void json(HttpExchange x,int status,Object payload)throws IOException{byte[]b=Json.stringify(payload).getBytes(StandardCharsets.UTF_8);x.getResponseHeaders().set("Content-Type","application/json; charset=utf-8");x.getResponseHeaders().set("Cache-Control","no-store");x.sendResponseHeaders(status,b.length);try(OutputStream o=x.getResponseBody()){o.write(b);}}
 public static void staticFile(HttpExchange x,Path webRoot)throws IOException{String p=x.getRequestURI().getPath();if(p.equals("/"))p="/index.html";Path file=webRoot.resolve(p.substring(1)).normalize();if(!file.startsWith(webRoot.normalize())||!Files.isRegularFile(file)){byte[]b="Not found".getBytes(StandardCharsets.UTF_8);x.sendResponseHeaders(404,b.length);x.getResponseBody().write(b);x.close();return;}String type=Files.probeContentType(file);if(type==null)type=p.endsWith(".css")?"text/css":p.endsWith(".js")?"application/javascript":"text/html";if(p.endsWith(".js"))type="application/javascript; charset=utf-8";else if(p.endsWith(".css"))type="text/css; charset=utf-8";else if(p.endsWith(".html"))type="text/html; charset=utf-8";byte[]b=Files.readAllBytes(file);x.getResponseHeaders().set("Content-Type",type);x.getResponseHeaders().set("Cache-Control","no-store");x.sendResponseHeaders(200,b.length);try(OutputStream o=x.getResponseBody()){o.write(b);}}
 public static String required(Map<String,String> f,String k){String v=f.getOrDefault(k,"").trim();if(v.isEmpty())throw new IllegalArgumentException("Please enter " + k + ".");return v;}
 public static int integer(Map<String,String>f,String k){try{return Integer.parseInt(required(f,k));}catch(NumberFormatException e){throw new IllegalArgumentException(k+" must be a number.");}}
 public static double decimal(Map<String,String>f,String k){try{return Double.parseDouble(required(f,k));}catch(NumberFormatException e){throw new IllegalArgumentException(k+" must be a number.");}}
}
