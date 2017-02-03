import java.util.Map;
import java.io.Serializable;

// 实现序列化类，使得能够用Socket传送该类的对象
public class Message implements Serializable {
  private static final long serialVersionUID = 1L;// 序列化UID，实现序列化类必须有的静态变量
  private String from;
  private String to;
  private String type;
  private String simpleMsg;
  private Map<String, String[]> routeTable;

  public Message() {}
  public Message(String from, String to, String type, String simpleMsg,
    Map<String, String[]> routeTable) {
    this.from = from;
    this.to = to;
    this.type = type;
    this.simpleMsg = simpleMsg;
    this.routeTable = routeTable;
  }

  public void setFrom(String from) {
    this.from = from;
  }
  public String getFrom() {
    return from;
  }

  public void setTo(String to) {
    this.to = to;
  }
  public String getTo() {
    return to;
  }

  public void setType(String type) {
    this.type = type;
  }
  public String getType() {
    return type;
  }

  public void setSimpleMsg(String simpleMsg) {
    this.simpleMsg = simpleMsg;
  }
  public String getSimpleMsg() {
    return simpleMsg;
  }

  public void setRouteTable(Map<String, String[]> routeTable) {
    this.routeTable = routeTable;
  }
  public Map<String, String[]> getRouteTable() {
    return routeTable;
  }
}