package com.glgames.game.ui;

public interface Action<T extends UIComponent> {
	public void doAction(T component, int x, int y);
}
