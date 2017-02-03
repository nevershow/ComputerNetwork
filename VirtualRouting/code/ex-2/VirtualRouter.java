import java.io.*;
import java.net.*;
import java.util.*;

public class VirtualRouter extends Thread {
  private ServerSocket receiver;
  private String me;
  private String controller;
  private GUI gui;

  public VirtualRouter(String ip_port) {
    try {
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
  public void run() {// 主线程，接收来自其他节点或者Controller的数据
    try {
      while (true) {
        receiveData(receiver.accept());
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  // 向controller获取所有在线节点，更新GUI网上邻居列表
  public void updateNeighborhood(String ip, String port) {
    try {
      controller = ip + ":" + port;
      Socket cSocket = new Socket(ip, Integer.valueOf(port));
      Message cMessage = new Message(me, controller, "CM", "NEIGHBORHOOD", null);
      ObjectOutputStream cSender = new ObjectOutputStream(cSocket.getOutputStream());
      ObjectInputStream cReader = new ObjectInputStream(cSocket.getInputStream());
      cSender.writeObject(cMessage);
      cSender.flush();
      gui.updateMessage("[CM]", "Ask Controller for neighborhood.");
      cMessage = (Message)cReader.readObject();
      cSender.close();
      cReader.close();
      cSocket.close();
      gui.updateNeighborhood(cMessage.getSimpleMsg().split("#"), me);
      gui.updateMessage("[CM]", "Receive neighborhood list from Controller.");
    } catch (IOException e) {
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
  }

  // 发送普通消息给其他节点
  public void sendSimpleMessage(String to, String simpleMsg) {
    Message message = new Message(me, to, "MSG", simpleMsg, null);
    sendData(to, message);
  }

  // 开辟线程发送消息实现消息并发传送，发送多条消息不会阻塞
  private void sendData(String to, Message message) {
    (new Thread() {
      @Override
      public void run() {
        try {
          // 向controller查询下一跳地址
          String[] c_ip_port = controller.split(":");
          Socket cSocket = new Socket(c_ip_port[0], Integer.valueOf(c_ip_port[1]));
          Message cMessage = new Message(me, controller, "CM", to, null);
          ObjectOutputStream cSender = new ObjectOutputStream(cSocket.getOutputStream());
          ObjectInputStream cReader = new ObjectInputStream(cSocket.getInputStream());
          cSender.writeObject(cMessage);
          cSender.flush();
          gui.updateMessage("[CM]", "Ask Controller for next hop.");
          cMessage = (Message)cReader.readObject();
          cSender.close();
          cReader.close();
          cSocket.close();
          String nextAddr = cMessage.getSimpleMsg();
          if (nextAddr == null) {// 如果没有下一跳，停止传送
            gui.updateMessage("[CM]", "No next hop.");
            return;
          }
          gui.updateMessage("[CM]", "Receive next hop \"" + nextAddr + "\" from Controller.");
          // 发送给下一跳
          String ip = nextAddr.split(":")[0];
          int port = Integer.valueOf(nextAddr.split(":")[1]);
          Socket socket = new Socket(ip, port);
          ObjectOutputStream sender = new ObjectOutputStream(socket.getOutputStream());
          sender.writeObject(message);
          sender.flush();
          sender.close();
          socket.close();
          if (message.getFrom().equals(me)) {
            gui.updateMessage("[MSG]", "Send \"" + message.getSimpleMsg() + "\" to " + nextAddr);
          } else {
            gui.updateMessage("[MSG]", "Forward \"" + message.getSimpleMsg() + "\" to " + nextAddr);
          }
        } catch (IOException e) {
          e.printStackTrace();
        } catch (ClassNotFoundException e) {
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
    String to = message.getTo();

    if (to.equals(me)) {// 发向自己，收取
      gui.updateMessage("[MSG]", "Receive \"" + message.getSimpleMsg() + "\" from " + message.getFrom());
    } else {// 发向他人，转发
      sendData(to, message);
    }
  }
}
