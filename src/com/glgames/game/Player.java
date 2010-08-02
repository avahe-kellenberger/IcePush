package com.glgames.game;


public class Player extends Object3D {
	public int type;
	public String username;

	// CODE IN COMMENTS HAS BEEN COMMENTED OUT
	//private int destX, destY;
	//private int startX, startY;
	//private long startTime;
	//private long endTime;
	
	public int deaths;
	public float bubbleAlpha;

	public Player(int type, String username) {
		super(type);
		this.type = type;
		this.username = username;
	}

	public void setPos(int x, int y) {
		baseX = x;
		baseZ = y;
	}

	// ALL CODE IN COMMENTS HAS BEEN COMMENTED OUT
	/*public void updatePos(int newX, int newY, int timeFromNow) {
		startX = x;
		startY = y;
		startTime = System.currentTimeMillis();
		if(timeFromNow < 0) {					// Time of < 0 is used to indicate unmotion
			x = newX;
			y = newY;
			endTime = timeFromNow;
		} else {
			endTime = timeFromNow + startTime;
			destX = newX;
			destY = newY;
		}
		makeObjectModelAndSpriteCoordinatesConsistentWithPlayerCoordinates();
	}

	public void handleMove() {
		if(endTime < 0) {
			return;
		}
		long now = System.currentTimeMillis();
		if(now >= endTime) {
			System.out.println("endtime has arrive, stopping: " + now + ", " + endTime);
			endTime = -1;
			return;
		}
		x = (int)(startX + ((destX - startX) * (now - startTime)) / (endTime - startTime));
		y = (int)(startY + ((destY - startY) * (now - startTime)) / (endTime - startTime));
		System.out.println("x=" + x + " y=" + y);
		makeObjectModelAndSpriteCoordinatesConsistentWithPlayerCoordinates();
	}

	private void makeObjectModelAndSpriteCoordinatesConsistentWithPlayerCoordinates() {
		sprite.x = x;
		sprite.y = y;
		model.baseX = x;
		model.baseZ = y;
	}*/
}