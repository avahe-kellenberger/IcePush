import net.threesided.shared.ILoader;

import java.applet.Applet;
import java.awt.*;
import java.net.URL;
import java.net.URLClassLoader;

public class loader extends Applet implements ILoader {
    private static Applet inst = null;

    public void run() {
        try {
            URLClassLoader ucl = new URLClassLoader(new URL[]{new URL(
                    "http://icepush.strictfp.com/play/IcePush.jar")});
            Class<?> icepush = ucl.loadClass("com.glgames.game.IcePush");
            inst = (Applet) icepush.newInstance();
            setLayout(new GridLayout(1, 0));
            add(inst);
            inst.init();
            inst.start();
            validate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void restart() {
        try {
            remove(inst);
            inst.stop();
            inst.destroy();
            System.gc();
            URLClassLoader ucl = new URLClassLoader(new URL[]{new URL(
                    "http://strictfp.com/icepush/IcePush.jar")});
            Class<?> icepush = ucl.loadClass("com.glgames.game.IcePush");
            inst = (Applet) icepush.newInstance();
            add(inst);
            inst.init();
            inst.start();
            validate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void init() {
        new Thread(this).start();
    }
}
