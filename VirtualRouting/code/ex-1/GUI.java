import org.jb2011.lnf.beautyeye.BeautyEyeLNFHelper;

import javax.swing.*;
import javax.swing.border.TitledBorder;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;
import java.util.HashSet;

public class GUI extends Thread {

    private JFrame frame;

    private JTextArea textArea;
    private JTextArea txt_msg;

    private JTextField txt_port;
    private JTextField txt_ip;
    private JButton btn_start;
    private JButton btn_send;
    private JPanel northPanel;

    private JPanel southPanel;
    private JPanel panel;
    private JScrollPane rightScroll;
    private JScrollPane leftScroll;
    private JScrollPane msgScroll;
    private JSplitPane centerSplit;
    private JSplitPane rightSplit;

    private JList neighbourList;
    private DefaultListModel listModel;

    private VirtualRouter virtualRouter;
    private Set<String> neighbors;

    public GUI() {
      neighbors = new HashSet<>();
    }

    public void setVirtualRouter(VirtualRouter virtualRouter) {
        this.virtualRouter = virtualRouter;
    }

    private static String[] DEFAULT_FONT  = new String[] {
        "Table.font"
        , "TableHeader.font"
        , "CheckBox.font"
        , "Tree.font"
        , "Viewport.font"
        , "ProgressBar.font"
        , "RadioButtonMenuItem.font"
        , "ToolBar.font"
        , "ColorChooser.font"
        , "ToggleButton.font"
        , "Panel.font"
        , "TextArea.font"
        , "Menu.font"
        , "TableHeader.font"
        , "OptionPane.font"
        , "MenuBar.font"
        , "Button.font"
        , "Label.font"
        , "PasswordField.font"
        , "ScrollPane.font"
        , "MenuItem.font"
        , "ToolTip.font"
        , "List.font"
        , "EditorPane.font"
        , "Table.font"
        , "TabbedPane.font"
        , "RadioButton.font"
        , "CheckBoxMenuItem.font"
        , "TextPane.font"
        , "PopupMenu.font"
        , "TitledBorder.font"
        , "ComboBox.font"
    };

    @Override
    public void run() {
        try {
            BeautyEyeLNFHelper.frameBorderStyle = BeautyEyeLNFHelper.FrameBorderStyle.osLookAndFeelDecorated;
            org.jb2011.lnf.beautyeye.BeautyEyeLNFHelper.launchBeautyEyeLNF();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        initialGUI();
        addListeners();
    }

    private void addListeners() {
        // 添加路由点击
        btn_start.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                String port = txt_port.getText();
                String ip = txt_ip.getText();

                if (ip.isEmpty() || port.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "IP地址和端口不能为空",
                            "Warning", JOptionPane.WARNING_MESSAGE);
                } else {
                    txt_port.setText("");
                    txt_ip.setText("");

                    virtualRouter.addNeighborhood(ip + ":" + port);
                }
            }
        });

        // 发送消息
        btn_send.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                String message = txt_msg.getText().trim();
                if (message.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "发送的消息不能为空",
                            "Warning", JOptionPane.WARNING_MESSAGE);
                } else {
                    String[] msg = message.split("#");
                    if (msg.length != 2) {
                        JOptionPane.showMessageDialog(frame, "发送的格式有误",
                                "Warning", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    if (msg[0].isEmpty() || msg[1].isEmpty()) {
                        JOptionPane.showMessageDialog(frame, "请填写目标地址和发送的消息",
                                "Warning", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    if (!neighbors.contains(msg[0])) {
                        JOptionPane.showMessageDialog(frame, "路由不存在",
                                "Warning", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    virtualRouter.sendSimpleMessage(msg[0], msg[1]);
                    txt_msg.setText("");
                }
            }
        });
    }

    // 呈现消息
    public void updateMessage(String msgType, String message) {
        textArea.append(msgType + " : " + message + "\n");
    }

    // 更新可达邻居列表
    public void updateNeighborhood(Set<String> neighbors) {
        this.neighbors = neighbors;
        listModel.removeAllElements();
        for (String neigh : neighbors) {
          listModel.addElement(neigh);
        }
    }

    private void initialGUI() {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        UIManager.put("RootPane.setupButtonVisible", false);

        // 调整默认字体
        for (int i = 0; i < DEFAULT_FONT.length; i++)
            UIManager.put(DEFAULT_FONT[i],
                    new Font(Font.SANS_SERIF, Font.PLAIN, 16));

        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setForeground(Color.gray);

        txt_ip = new JTextField("");
        txt_port = new JTextField("");

        btn_start = new JButton("添加");
        btn_send = new JButton("发送");

        listModel = new DefaultListModel();
        neighbourList = new JList(listModel);

        // "添加路由"布局
        northPanel = new JPanel();

        GridBagLayout gridBagLayout = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();

        northPanel.setLayout(gridBagLayout);

        constraints.insets = new Insets(0, 5, 0, 5);
        constraints.fill = GridBagConstraints.BOTH;

        JLabel label;

        constraints.weightx = 0.1;
        label = new JLabel("IP");
        gridBagLayout.setConstraints(label, constraints);
        northPanel.add(label);

        constraints.weightx = 3.0;
        gridBagLayout.setConstraints(txt_ip, constraints);
        northPanel.add(txt_ip);

        constraints.weightx = 0.1;
        label = new JLabel("端口");
        gridBagLayout.setConstraints(label, constraints);
        northPanel.add(label);

        constraints.weightx = 3.0;
        gridBagLayout.setConstraints(txt_port, constraints);
        northPanel.add(txt_port);

        gridBagLayout.setConstraints(btn_start, constraints);
        northPanel.add(btn_start);

        northPanel.setBorder(new TitledBorder("添加直连路由"));

        rightScroll = new JScrollPane(textArea);
        rightScroll.setBorder(new TitledBorder("消息记录"));

        leftScroll = new JScrollPane(neighbourList);
        leftScroll.setBorder(new TitledBorder("网上邻居"));

        txt_msg = new JTextArea();
        msgScroll = new JScrollPane(txt_msg);

        southPanel = new JPanel(new BorderLayout());
        southPanel.add(msgScroll, "Center");

        // 底部浮动布局
        panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        btn_send.setMargin(new Insets(5, 20, 5, 20));
        panel.add(btn_send);

        southPanel.add(panel, "South");
        southPanel.setBorder(new TitledBorder("发送"));

        // 右边上下分割
        rightSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, rightScroll, southPanel);
        rightSplit.setDividerLocation(350);

        // 左右两边分割
        centerSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftScroll, rightSplit);
        centerSplit.setDividerLocation(250);

        // 整体框架
        frame = new JFrame("Router");
        frame.setSize(960, 720);

        // 上中布局
        frame.setLayout(new BorderLayout());
        frame.add(northPanel, "North");
        frame.add(centerSplit, "Center");

        int screenWidth = toolkit.getScreenSize().width;
        int screenHeight = toolkit.getScreenSize().height;

        // 在屏幕中间显示
        frame.setLocation((screenWidth - frame.getWidth()) / 2,
                          (screenHeight - frame.getHeight()) / 2);

        frame.setVisible(true);
    }
}
