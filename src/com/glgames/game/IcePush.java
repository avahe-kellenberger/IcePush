package com.glgames.game;

public class IcePush {
	public static boolean DEBUG = false;
	
	public static void main(String[] args) {
		GameEngine.init();
		GameEngine.run();
		GameEngine.cleanup();
	}
}
