package com.glgames.shared;

public class Opcodes {
	// World server
	public static final String WORLDSERVER = "99.198.122.53";
	public static final int WORLDPORT = 2346;
	public static final int WORLD_REQUEST = 1;
	public static final int WORLD_SEND = 2;
	
	// Login protocol
	public static final int VERSION = 105;
	public static final int BAD_VERSION = 1;
	public static final int USER_IN_USE = 2;
	public static final int TOO_MANY_PL = 3;
	public static final int SUCCESS_LOG = 4;
	
	// Packet opcodes Server to Client
	public static final int NEW_PLAYER = 5;
	public static final int PLAYER_MOVED = 6;
	public static final int KEEP_ALIVE = 7;
	public static final int PLAYER_LOGGED_OUT = 11;
	public static final int PLAYER_DIED = 12;
	public static final int SET_CAN_MOVE = 13;
	public static final int PLAYER_STOPPED_MOVING = 15;
	// Client to Server
	public static final int MOVE_REQUEST = 8;
	public static final int END_MOVE = 9;
	public static final int LOGOUT = 10;
	public static final int PING = 14;
	
	// Player types
	public static final int TREE = 0;
	public static final int SNOWMAN = 1;
	
	// Movement directions
	public static final int LEFT = 1 << 0;
	public static final int RIGHT = 1 << 1;
	public static final int UP = 1 << 2;
	public static final int DOWN = 1 << 3;
}
