package com.glgames.game;

import java.awt.AWTEvent;
import java.awt.EventQueue;
import java.awt.Frame;

public class GameFrame extends Frame {
	private static final long serialVersionUID = 1L;

	public Thread WIDTH, HEIGHT;

	public GameFrame() {
		super("IcePush");

		setFocusTraversalKeysEnabled(false);
		enableEvents(AWTEvent.WINDOW_EVENT_MASK);

		add(IcePush.instance);
		setSize(IcePush.WIDTH, IcePush.HEIGHT);
		setResizable(false);
		setVisible(true);
		pack();
	}

	public void setRenderer(final Renderer r) {
		IcePush.stable = false;
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				if (IcePush.renderer != null)
					remove(IcePush.instance);
				
				add(IcePush.instance);
				validate();
				r.initGraphics();
				IcePush.buffGraphics = r.getBufferGraphics();
				IcePush.stable = true;
			}
		});
	}
}
