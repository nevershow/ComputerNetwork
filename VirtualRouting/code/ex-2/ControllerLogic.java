import java.util.*;
import java.io.*;

public class ControllerLogic {
  public HashMap<String, Integer> addr2int;
  public HashMap<Integer, String> int2addr;
  private ArrayList< ArrayList<Integer> > mNetwork;
  private int mNetworkSize;

  public ControllerLogic() {
    addr2int = new HashMap<>();
    int2addr = new HashMap<>();
    mNetwork = new ArrayList<>();

    addr2int.put("127.0.0.1:8080", 0);
    addr2int.put("127.0.0.1:8081", 1);
    addr2int.put("127.0.0.1:8082", 2);
    addr2int.put("127.0.0.1:8083", 3);
    addr2int.put("127.0.0.1:8084", 4);

    int2addr.put(0, "127.0.0.1:8080");
    int2addr.put(1, "127.0.0.1:8081");
    int2addr.put(2, "127.0.0.1:8082");
    int2addr.put(3, "127.0.0.1:8083");
    int2addr.put(4, "127.0.0.1:8084");

    mNetworkSize = 5;
    for(int i = 0; i < mNetworkSize; i++) {
      ArrayList<Integer> templist =  new ArrayList<Integer>();
      for (int j = 0; j < mNetworkSize; j++) {
        templist.add(Integer.MAX_VALUE);
      }
      mNetwork.add(templist);
    }

    // topo
    mNetwork.get(0).set(0, 0);
    mNetwork.get(0).set(2, 1);
    mNetwork.get(0).set(4, 1);
    mNetwork.get(1).set(1, 0);
    mNetwork.get(1).set(3, 1);
    mNetwork.get(1).set(4, 1);
    mNetwork.get(2).set(0, 1);
    mNetwork.get(2).set(2, 0);
    mNetwork.get(2).set(3, 1);
    mNetwork.get(3).set(1, 1);
    mNetwork.get(3).set(2, 2);
    mNetwork.get(3).set(3, 0);
    mNetwork.get(4).set(0, 1);
    mNetwork.get(4).set(1, 1);
    mNetwork.get(4).set(4, 0);
  }

  public ArrayList<Tableitem> getLinkState(int index) {
    ArrayList<Tableitem> mLinkStates = new ArrayList<>();
    for (int i = 0; i < mNetworkSize; i++) {
      Tableitem item = new Tableitem();
      item.cost = mNetwork.get(index).get(i);
      if (item.cost != Integer.MAX_VALUE) {
        item.through = index;
        item.router = i;
      }
      mLinkStates.add(item);
    }

    HashSet<Integer> Nset = new HashSet<>();
    Nset.add(index);

    while(true) {
      int w = selectMinCostNode(Nset ,mLinkStates);
      if (w == -1) break;
      Nset.add(w);
      ArrayList<Integer> neighbour = getNeighbour(Nset, w);
      if (neighbour.size() == 0) continue;
      for (int i = 0; i < neighbour.size(); i++) {
        int v = neighbour.get(i);
        int dv = mLinkStates.get(v).cost;
        int dw = mLinkStates.get(w).cost;
        int wv = mNetwork.get(w).get(v);
        if (dw + wv < dv) {
          mLinkStates.get(v).cost = dw+wv;
          mLinkStates.get(v).through = w;
          mLinkStates.get(v).router = mLinkStates.get(w).router;
        }
      }
    }

    return mLinkStates;
  }

  private int selectMinCostNode(HashSet<Integer> Nset, ArrayList<Tableitem> mLinkStates) {
    int min = -1;
    int minCost = Integer.MAX_VALUE;
    for (int i = 0; i < mLinkStates.size(); i++) {
      if (!Nset.contains(i)) {
        if (mLinkStates.get(i).cost < minCost) {
          minCost = mLinkStates.get(i).cost;
          min = i;
        }
      }
    }
    return min;
  }

  private ArrayList<Integer> getNeighbour(HashSet<Integer> Nset, int index) {
    ArrayList<Integer> neighbour = new ArrayList<>();
    for (int i = 0; i < mNetworkSize; i++) {
      int dist = mNetwork.get(index).get(i);
      if (dist > 0 && dist != Integer.MAX_VALUE && !Nset.contains(i)) neighbour.add(i);
    }
    return neighbour;
  }

  public static class Tableitem {
    public int router;
    public int through;
    public int cost;
  }
}
