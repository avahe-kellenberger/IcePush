package net.threesided.shared;

public class Constants {
	// World server
	//public static final String WORLDSERVER = "99.198.122.53";
	//public static final int WORLDPORT = 2346;
	public static final int NUM_PLAYERS_REQUEST = 0;
	public static final int NEW_SERVER = 1;
	public static final int NUM_PLAYERS_NOTIFY = 2;
	// Login protocol
	public static final int VERSION = 106;
	public static final int FAILURE = 1;
	public static final int SUCCESS_LOG = 2;

	// Packet opcodes Server to Client
	public static final int NEW_PLAYER = 5;
	public static final int PLAYER_MOVED = 6;
	public static final int KEEP_ALIVE = 7;
	public static final int PLAYER_LOGGED_OUT = 11;
	public static final int PLAYER_DIED = 12;
	//public static final int PLAYER_STOPPED_MOVING = 15;
	public static final int NEW_CHAT_MESSAGE = 17;
	public static final int UPDATE = 19;
	public static final int UPDATE_TIME = 18;

	// Client to Server
	public static final int MOVE_REQUEST = 8;
	public static final int END_MOVE = 9;
	public static final int LOGOUT = 10;
	public static final int PING = 14;
	public static final int CHAT_REQUEST = 16;
	public static final int PROJECTILE_REQUEST = 15;

	// Object types
	public static final int TREE = 0;
	public static final int SNOWMAN = 1;
	public static final int BALL = 2;

	// Movement directions
	//public static final int LEFT = 1 << 0;
	//public static final int RIGHT = 1 << 1;
	//public static final int UP = 1 << 2;
	//public static final int DOWN = 1 << 3;
}
