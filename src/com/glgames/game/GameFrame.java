package com.glgames.game;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;


public class GameFrame extends JFrame {
	private static final long serialVersionUID = 1L;
	
	private Renderer renderer;
	
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
		
		if(GameObjects.GRAPHICS_MODE == GameObjects.TWO_D)
			renderer = new Renderer2D();
		else
			renderer = new Renderer3D();
		renderer.setFocusTraversalKeysEnabled(false);
		renderer.addKeyListener(new KeyHandler());
		renderer.addMouseListener(new MouseHandler());
		
		add(renderer);
		setSize(GameFrame.WIDTH, GameFrame.HEIGHT);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		setResizable(false);
		setVisible(true);
		
		renderer.initGraphics();
		renderer.requestFocus();
	}

	public Renderer getRenderer() {
		return renderer;
	}
}
