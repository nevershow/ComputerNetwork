import java.util.*;
import java.io.*;
import java.net.*;

public class ControllerMain {
  public static void main(String[] args) {
    try {
      System.out.println("请输入Controller的IP端口<xxx.xxx.xxx.xxx:xxx>");
      BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
      String controllerAddr = in.readLine();
      in.close();
      ControllerLogic controllerLogic = new ControllerLogic();
      ServerSocket serverSocket = new ServerSocket(Integer.valueOf(controllerAddr.split(":")[1]));
      System.out.println("[Controller] listen at " + controllerAddr);

      while (true) {// 接收来自虚拟路由的请求，开辟线程进行处理
        (new HandlerThread(serverSocket.accept(),
          controllerLogic, controllerAddr)).start();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  // 处理请求的线程类
  public static class HandlerThread extends Thread {
    Socket socket;
    String controllerAddr;
    ControllerLogic controllerLogic;
    HandlerThread(Socket socket, ControllerLogic controllerLogic, String controllerAddr) {
      this.socket = socket;
      this.controllerLogic = controllerLogic;
      this.controllerAddr = controllerAddr;
    }

    @Override
    public void run() {
      try {
        ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
        Message message = (Message)ois.readObject();

        if (message.getType().equals("CM")) {
          String from = message.getFrom();
          if (message.getSimpleMsg().equals("NEIGHBORHOOD")) {// 请求查询所有可达节点
            String neighbors = "";
            boolean begin = true;
            for (String neigh : controllerLogic.addr2int.keySet()) {
              if (begin) {
                neighbors += neigh;
                begin = false;
              } else {
                neighbors += ("#" + neigh);
              }
            }
            message = new Message(controllerAddr, from, "CM", neighbors, null);
          }

          else {// 请求查询下一跳地址
            String destAddr = message.getSimpleMsg();
            int src = controllerLogic.addr2int.get(from);
            int dest = controllerLogic.addr2int.get(destAddr);
            ArrayList<ControllerLogic.Tableitem> mLinkStates = controllerLogic.getLinkState(src);
            int next = mLinkStates.get(dest).router;
            String nextAddr = controllerLogic.int2addr.get(next);
            if (nextAddr.equals(from)) nextAddr = null;
            message = new Message(controllerAddr, from, "CM", nextAddr, null);
          }

          oos.writeObject(message);
          oos.flush();
        }

        ois.close();
        oos.close();
        socket.close();
      } catch (IOException e) {
        e.printStackTrace();
      } catch (ClassNotFoundException e) {
        e.printStackTrace();
      }
    }
  }
}
