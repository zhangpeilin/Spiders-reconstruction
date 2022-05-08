package cn.zpl.frame;

import javax.swing.JFrame;
import javax.swing.JLabel;
import java.util.HashMap;
import java.util.Map;

public abstract class MyJFrame extends JFrame {
    protected Map<String, String> info = new HashMap<>();
    public abstract JLabel getScreen();
    public abstract JFrame init();
    public  void updateInfo(String key, String infomation){
        info.put(key, infomation);
    }
    public  void removeInfo(String key){
        info.remove(key);
    }

    public void clearInfo() {
        info.clear();
    }
}
