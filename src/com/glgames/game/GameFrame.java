package com.glgames.game;

import java.awt.AWTEvent;
import java.awt.EventQueue;
import java.awt.Frame;

public class GameFrame extends Frame {
	private static final long serialVersionUID = 1L;

	public Renderer renderer;

	public static final int WIDTH = 450;
	public static final int HEIGHT = 600;

	public GameFrame() {
		super("IcePush");

		setFocusTraversalKeysEnabled(false);
		enableEvents(AWTEvent.WINDOW_EVENT_MASK);

		if (GameObjects.GRAPHICS_MODE == GameObjects.SOFTWARE_2D)
			renderer = new Renderer2D();
		else
			renderer = new Renderer3D();

		add(renderer.getCanvas());
		setSize(GameFrame.WIDTH, GameFrame.HEIGHT);
		setResizable(false);
		setVisible(true);

		renderer.initGraphics();
	}

	public void setRenderer(final Renderer r) {
		IcePush.stable = false;
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				if (renderer != null)
					remove(renderer.getCanvas());
				
				add(r.getCanvas());
				validate();

				renderer = r;
				r.initGraphics();
				IcePush.buffGraphics = r.getBufferGraphics();
				IcePush.stable = true;
			}
		});
	}
}
