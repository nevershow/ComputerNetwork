import java.io.*;
import java.net.*;
import java.util.*;

public class VirtualRouter extends Thread {
  private ServerSocket receiver;
  private List<String> neighborhood;
  private Map<String, String[]> routeTable;
  private String me;
  private GUI gui;

  public VirtualRouter(String ip_port) {
    try {
      neighborhood = new ArrayList<>();
      routeTable = new LinkedHashMap<>();
      receiver = new ServerSocket(Integer.valueOf(ip_port.split(":")[1]));
      me = ip_port;
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void setGUI(GUI gui) {
    this.gui = gui;
  }

  @Override
  public void run() {// 主线程，接收来自其他节点的数据
    try {
      while (true) {
        receiveData(receiver.accept());
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  // 发送普通消息给其他节点
  public void sendSimpleMessage(String to, String simpleMsg) {
    Message message = new Message(me, to, "MSG", simpleMsg, null);
    sendData(routeTable.get(to)[0], message);
    gui.updateMessage("[MSG]", "Send \"" + simpleMsg + "\" to " + to);
  }

  // 发送自己的路由表给所有邻居
  private void sendRouteTableToAllNeighbor() {
    for (String neigbhor : neighborhood) {
      Message newMsg = new Message(me, neigbhor, "RT", "", routeTable);
      sendData(neigbhor, newMsg);
      gui.updateMessage("[RT]", "Send route table to " + neigbhor);
    }
  }

  // 开辟线程发送消息实现消息并发传送，发送多条消息不会阻塞
  private void sendData(String to, Message message) {
    (new Thread() {
      @Override
      public void run() {
        try {
          String ip = to.split(":")[0];
          int port = Integer.valueOf(to.split(":")[1]);
          Socket socket = new Socket(ip, port);
          ObjectOutputStream sender = new ObjectOutputStream(socket.getOutputStream());
          sender.writeObject(message);
          sender.flush();
          sender.close();
          socket.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }).start();
  }

  // 接收到其他节点发过来的普通消息之后进行的相应处理
  private void receiveData(Socket socket) throws IOException {
    ObjectInputStream reader = new ObjectInputStream(socket.getInputStream());
    Message message = null;
    try {
      message = (Message)reader.readObject();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
    reader.close();
    socket.close();
    if (message == null) return;
    String neigbhor = message.getFrom();

    if (message.getType().equals("RT")) {// 收到邻居发送的路由表
      gui.updateMessage("[RT]", "Receive route table from " + neigbhor);
      boolean updated = processRouteTable(message.getRouteTable(), neigbhor);
      if (updated) {// 路由表更新，告知邻居
        gui.updateNeighborhood(routeTable.keySet());
        sendRouteTableToAllNeighbor();
      }
    } else if (message.getType().equals("MSG")) {// 收到普通消息
      if (message.getTo().equals(me)) {// 发向自己，收取
        gui.updateMessage("[MSG]", "Receive \"" + message.getSimpleMsg() + "\" from " + neigbhor);
      } else {// 发向他人，转发
        String to = routeTable.get(message.getTo())[0];
        gui.updateMessage("[MSG]", "Forward \"" + message.getSimpleMsg() + "\" to " + to);
        sendData(to, message);
      }
    }
  }

  // 添加直连路由
  public void addNeighborhood(String neigbhor) {
    neighborhood.add(neigbhor);
    routeTable.put(neigbhor, new String[] {neigbhor, "1"});
    sendRouteTableToAllNeighbor();
    gui.updateNeighborhood(routeTable.keySet());
  }

  // 收到邻居的路由表之后进行处理，并根据情况对自己的路由表进行更新
  private boolean processRouteTable(Map<String, String[]> neighborTable, String neigbhor) {
    boolean isChange = false;
    if (!routeTable.containsKey(neigbhor)) {
      neighborhood.add(neigbhor);
      routeTable.put(neigbhor, new String[] {neigbhor, "1"});
      isChange = true;
    }
    String[] neighInfo = routeTable.get(neigbhor);
    int neighDis = Integer.valueOf(neighInfo[1]);
    Set<String> neighKeys = neighborTable.keySet();

    for (String neighKey: neighKeys) {
      if (neighKey.equals(me)) continue;
      String[] neighKeyValue = neighborTable.get(neighKey);
      int dis = Integer.valueOf(neighKeyValue[1]);
      if (!routeTable.containsKey(neighKey)) {// 原本不可达，通过该邻居变成可达
        routeTable.put(neighKey, new String[]{neigbhor, String.valueOf(dis + neighDis)});
        isChange = true;
      } else {// 原本可达
        int myDis = Integer.valueOf(routeTable.get(neighKey)[1]);
        if (dis + neighDis < myDis) {// 通过该邻居变得更近
          routeTable.put(neighKey, new String[]{neigbhor, String.valueOf(dis + neighDis)});
          isChange = true;
        }
      }
    }
    return isChange;
  }
}
