package com.glgames.game;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;


public class GameFrame extends JFrame {
	private static final long serialVersionUID = 1L;
	
	private Image buffer;
	private Graphics bufferGraphics;

	public static final int WIDTH = 450;
	public static final int HEIGHT = 600;

	public GameFrame() {
		super("IcePush");
		
		setFocusTraversalKeysEnabled(false);
		addKeyListener(new KeyHandler());
		addMouseListener(new MouseHandler());
		
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				GameEngine.running = false;
			}
		});
		
		setSize(GameFrame.WIDTH, GameFrame.HEIGHT);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		setResizable(false);
		setVisible(true);
		
		buffer = createImage(GameFrame.WIDTH, GameFrame.HEIGHT);
		bufferGraphics = buffer.getGraphics();
	}
	
	public Graphics getBufferGraphics() {
		return bufferGraphics;
	}
	
	public void paint(Graphics g) {
		g.drawImage(buffer, 0, 0, this);
	}
}
