public class VRMain {
  public static void main(String args[]) {
    VirtualRouter virtualRouter = new VirtualRouter(args[0]);
    GUI gui = new GUI();
    virtualRouter.setGUI(gui);
    gui.setVirtualRouter(virtualRouter);
    virtualRouter.start();
    gui.start();
  }
}