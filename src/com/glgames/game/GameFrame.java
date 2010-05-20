package com.glgames.game;

import java.awt.*;
import java.applet.*;
import java.awt.event.*;


public class GameFrame extends java.awt.Frame {
	private static final long serialVersionUID = 1L;
	
	public Renderer renderer;
	
	public static final int WIDTH = 450;
	public static final int HEIGHT = 600;

	public GameFrame() {
		super("IcePush");
		
		setFocusTraversalKeysEnabled(false);
		enableEvents(AWTEvent.WINDOW_EVENT_MASK);
		
		if(GameObjects.GRAPHICS_MODE == GameObjects.SOFTWARE_2D)
			renderer = new Renderer2D();
		else
			renderer = new Renderer3D();

	//	renderer.setFocusTraversalKeysEnabled(false);
	//	renderer.addKeyListener(new KeyHandler());
	//	renderer.addMouseListener(new MouseHandler());
		
	//	add(renderer);
		setSize(GameFrame.WIDTH, GameFrame.HEIGHT);
	//	setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		setResizable(false);
		setVisible(true);
		
		renderer.initGraphics();
		renderer.requestFocus();
	}

	public void setRenderer(final Renderer r) {
		IcePush.stable = false;
		//SwingUtilities.invokeLater(new Runnable() {
		//	public void run() {
				if(renderer != null)
					remove(renderer);
				r.setFocusTraversalKeysEnabled(false);
				r.addKeyListener(new KeyHandler());
				r.addMouseListener(new MouseHandler());
				
				add(r);
				validate();
				
				r.initGraphics();
				r.requestFocus();
				
				renderer = r;
				IcePush.buffGraphics = r.getBufferGraphics();
				IcePush.stable = true;
				System.out.println("Graphics mode set to " + GameObjects.GRAPHICS_MODE);
		//	}
		//});
	}
}
