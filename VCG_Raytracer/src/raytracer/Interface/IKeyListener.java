package raytracer.Interface;

import java.awt.event.KeyEvent;

import raytracer.RenderMode;

/// this class/interface will allow a class to receive key input to switch the
/// render modes
public class IKeyListener {
    
    public IKeyListener(){

    };

    public void UpdateRenderMode(RenderMode mode) {

    }

    public void Reload() {
        
    }

    public void ReceiveKey(KeyEvent key) {
        if (isNumber(key)) {

            ReceiveKeyNumber(ToNumber(key));
        }
    }
    
    ///checks if the received key event is a number 
    public static boolean isNumber(KeyEvent key) {
        /*if (KeyEvent.VK_1 == key.getKeyCode() ||
                KeyEvent.VK_2 == key.getKeyCode() ||
                KeyEvent.VK_3 == key.getKeyCode() ||
                KeyEvent.VK_4 == key.getKeyCode() ||
                KeyEvent.VK_5 == key.getKeyCode() ||
                KeyEvent.VK_6 == key.getKeyCode() ||
                KeyEvent.VK_7 == key.getKeyCode() ||
                KeyEvent.VK_8 == key.getKeyCode() ||
                KeyEvent.VK_9 == key.getKeyCode() ||
                KeyEvent.VK_0 == key.getKeyCode()) {
            return true;
        }
        return false;    
        */
        int keyInt = ToNumber(key);
        return keyInt != -1;
    }

    /// converts the key event to a number if is a number key
    public static int ToNumber(KeyEvent key) {
        if(KeyEvent.VK_1 == key.getKeyCode())
            return 1;
        if(KeyEvent.VK_2 == key.getKeyCode())
            return 2;
        if(KeyEvent.VK_3 == key.getKeyCode())
            return 3;
        if(KeyEvent.VK_4 == key.getKeyCode())
            return 4;
        if(KeyEvent.VK_5 == key.getKeyCode())
            return 5;
        if(KeyEvent.VK_6 == key.getKeyCode())
            return 6;
        if(KeyEvent.VK_7 == key.getKeyCode())
            return 7;
        if(KeyEvent.VK_8 == key.getKeyCode())
            return 8;
        if(KeyEvent.VK_9 == key.getKeyCode())
            return 9;
        if(KeyEvent.VK_0 == key.getKeyCode())
            return 0;
        return -1;
    }



    //to be overriden by subclass if a reaction to keys is wanted
    public void ReceiveKeyNumber(int key) {
        
    }

}
