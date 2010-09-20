package com.glgames.ui;

public interface Action<T extends UIComponent> {
	public void doAction(T component, int x, int y);
}
