package com.glgames.game;

import java.awt.AWTEvent;
import java.awt.Frame;

public class GameFrame extends Frame {
	private static final long serialVersionUID = 1L;
	
	public GameFrame() {
		super("IcePush");

		setFocusTraversalKeysEnabled(false);
		enableEvents(AWTEvent.WINDOW_EVENT_MASK);
		
		add(IcePush.instance);
		setResizable(false);
		setVisible(true);
		pack();
	}
}
