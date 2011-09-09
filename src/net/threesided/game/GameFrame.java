package net.threesided.game;

import java.awt.Frame;
import java.awt.AWTEvent;
import java.awt.event.WindowEvent;


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

	public void processWindowEvent(WindowEvent we) {
		super.processWindowEvent(we);
		if(we.getID() == WindowEvent.WINDOW_CLOSING) {
			if(IcePush.instance != null)
				IcePush.instance.stop();
			dispose();
			//System.exit(0);
		}
	}
}
