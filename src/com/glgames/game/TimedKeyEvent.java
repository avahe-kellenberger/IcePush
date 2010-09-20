package com.glgames.game;

import java.awt.event.KeyEvent;

class TimedKeyEvent {
	final KeyEvent event;
	final long time;

	TimedKeyEvent(KeyEvent ke) {
		event = ke;
		time = System.currentTimeMillis();
	}
}