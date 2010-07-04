package com.glgames.loader;

import java.applet.Applet;
import java.awt.GridLayout;
import java.net.URL;
import java.net.URLClassLoader;

public class loader extends Applet implements Runnable {
	public void run() {
		try {
			URLClassLoader ucl = new URLClassLoader(new URL[] { new URL(
					"http://icepush.strictfp.com/play/IcePush.jar") });
			Class<?> icepush = ucl.loadClass("com.glgames.game.IcePush");
			Applet inst = (Applet) icepush.newInstance();
			setLayout(new GridLayout(1, 0));
			add(inst);
			inst.init();
			inst.start();
			validate();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void init() {
		new Thread(this).start();
	}
}
