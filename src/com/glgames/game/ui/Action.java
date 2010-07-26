package com.glgames.game.ui;

public interface Action<Target extends UIComponent> {
	public void action(Target t, int x, int y);
}
