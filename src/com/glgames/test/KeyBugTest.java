package com.glgames.test;

import java.awt.Frame;
import static java.awt.AWTEvent.*;
import java.awt.event.*;
import static java.awt.event.KeyEvent.*;
import static java.awt.event.WindowEvent.*;

public class KeyBugTest extends Frame {

	/*
	THE PROBLEM HERE IS THAT KEY EVENTS DO NOT WORK RIGHT WITH OUR MOVEMENT SYSTEM
	ON LINUX BECAUSE OF A BUG IN THE EVENT PROCESSORS (http://bugs.sun.com/view_bug.do?bug_id=4153069)
	THE CURRENT WORK AROUND IS TO USE A TIMER TO REDIRECT THE EVENTS PROPERLY
	BUT THE TIMER IS NOT TERMINATING AS IT SHOULD WHEN IT HAS BEEN STOPPED DURING APPLICATION SHUTDOWN
	WHEN EXECUTED IN APPLICATION MODE THIS ISN'T AN ISSUE SINCE SYSTEM.EXIT WILL DESTROY EVERYTHING ANYWAY
	HOWEVER WHEN RUN AS AN APPLET, IN SOME BROWSERS CALLING SYSTEM.EXIT WILL JUST TERMINATE EVERYTHING
	THIS IS VERY BAD AND WE ARE THUSLY LOOKING FOR A LESS SHITTY SOLUTION, WHENCE THE CREATION OF THIS TEST.
	*/

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private long lastKETime;

	public static void main(String thusly[]) {
		KeyBugTest kbg = new KeyBugTest();
		kbg.setSize(600, 100);
		kbg.setVisible(true);
	}

	KeyBugTest() {
		super("PRESS LOTS OF KEYS AND SEND THE OUTPUT TO _^_");
		enableEvents(WINDOW_EVENT_MASK | KEY_EVENT_MASK);
	}

	public void processKeyEvent(KeyEvent ke) {
		String type = "KEY_PRESSED";
		switch(ke.getID()) {
			case KEY_TYPED:
				type = "KEY_TYPED";
				break;
			case KEY_RELEASED:
				type = "KEY_RELEASED";
				break;
		}
		long now = System.currentTimeMillis();
		System.out.println("KEY EVENT: " + type + " DELAY FROM PREVIOUS: " + (now - lastKETime));
		lastKETime = now;
	}

	public void processWindowEvent(WindowEvent we) {
		if(we.getID() == WINDOW_CLOSING) dispose();
	}
}