package net.threesided.game;

import java.awt.Frame;
import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.event.WindowEvent;


public class GameFrame extends Frame {
	private static final long serialVersionUID = 1L;
	
	public GameFrame(String title, Component c) {
		super(title);

		setFocusTraversalKeysEnabled(false);
		enableEvents(AWTEvent.WINDOW_EVENT_MASK);
		
		add(c);
		setResizable(false);
		setVisible(true);
		pack();
	}

	public void processWindowEvent(WindowEvent we) {
		super.processWindowEvent(we);
		if(we.getID() == WindowEvent.WINDOW_CLOSING) dispose();
	}
}
