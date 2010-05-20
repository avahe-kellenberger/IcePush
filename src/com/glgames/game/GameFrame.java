package com.glgames.game;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;


public class GameFrame extends JFrame {
	private static final long serialVersionUID = 1L;
	
	public Renderer renderer;
	
	public static final int WIDTH = 450;
	public static final int HEIGHT = 600;

	public GameFrame() {
		super("IcePush");
		
		setFocusTraversalKeysEnabled(false);
		
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				GameEngine.running = false;
			}
		});
		
		if(GameObjects.GRAPHICS_MODE == GameObjects.SOFTWARE_2D)
			renderer = new Renderer2D();
		else
			renderer = new Renderer3D();
		
		add(renderer.getCanvas());
		setSize(GameFrame.WIDTH, GameFrame.HEIGHT);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		setResizable(false);
		setVisible(true);
		
		renderer.initGraphics();
	}

	public void setRenderer(final Renderer r) {
		if(r == null)
			throw new IllegalStateException("wtf");
		GameEngine.stable = false;
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if(renderer != null)
					remove(renderer.getCanvas());
				
				add(r.getCanvas());
				validate();
				
				r.initGraphics();
				
				renderer = r;
				GameEngine.buffGraphics = r.getBufferGraphics();
				GameEngine.stable = true;
				System.out.println("Graphics mode set to " + GameObjects.GRAPHICS_MODE);
			}
		});
	}
}
