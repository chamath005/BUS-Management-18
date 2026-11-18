package common;
import java.util.*;
public final class Json {
 private Json(){}
 public static String stringify(Object value){
  if(value==null)return "null";
  if(value instanceof String s)return quote(s);
  if(value instanceof Number || value instanceof Boolean)return value.toString();
  if(value instanceof Map<?,?> map){StringJoiner j=new StringJoiner(",","{","}"); for(var e:map.entrySet()) j.add(quote(String.valueOf(e.getKey()))+":"+stringify(e.getValue())); return j.toString();}
  if(value instanceof Iterable<?> it){StringJoiner j=new StringJoiner(",","[","]"); for(Object x:it)j.add(stringify(x));return j.toString();}
  return quote(value.toString());
 }
 private static String quote(String s){StringBuilder b=new StringBuilder("\""); for(char c:s.toCharArray()){switch(c){case '\\'->b.append("\\\\");case '"'->b.append("\\\"");case '\n'->b.append("\\n");case '\r'->b.append("\\r");case '\t'->b.append("\\t");default->{if(c<32)b.append(String.format("\\u%04x",(int)c));else b.append(c);}}}return b.append('"').toString();}
 public static Map<String,Object> obj(Object... kv){Map<String,Object> m=new LinkedHashMap<>();for(int i=0;i+1<kv.length;i+=2)m.put(String.valueOf(kv[i]),kv[i+1]);return m;}
}
