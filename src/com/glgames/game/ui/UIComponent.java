package com.glgames.game.ui;

import java.awt.Color;

import com.glgames.game.Renderer;
import com.glgames.game.IcePush;
import com.glgames.game.NetworkHandler;
import com.glgames.shared.FileBuffer;

public class UIComponent {
	public static UIComponent[] interfaces;
	public static Action<UIComponent>[] actions;
	
	public int x, y;
	public int width, height;
	public int visibleDuring;
	
	short id;
	short actionID = -1;
	short parentID = -1;
	UIComponent parent;

	UIComponent() { }
	
	@SuppressWarnings("unchecked")
	public static void loadUI() {
        short numInter = 7;
        interfaces = new UIComponent[numInter];

        UIComponent dataComponent = new UIComponent();
        dataComponent.id = 0;
        dataComponent.parentID = -1;
        dataComponent.x = 315;
        dataComponent.y = 275;
        dataComponent.visibleDuring = IcePush.WELCOME;

        TextBox serverTextBox = new TextBox();
        serverTextBox.id = 1;
        serverTextBox.parentID = dataComponent.id;
        serverTextBox.x = 0;
        serverTextBox.y = 0;
        serverTextBox.visibleDuring = IcePush.WELCOME;
        serverTextBox.isFocused = false;
        serverTextBox.caption = "Server: ";
        serverTextBox.value = "strictfp.com";
        
        TextBox usernameTextBox = new TextBox();
        usernameTextBox.id = 2;
        usernameTextBox.parentID = dataComponent.id;
        usernameTextBox.x = 0;
        usernameTextBox.y = 25;
        usernameTextBox.visibleDuring = IcePush.WELCOME;
        usernameTextBox.isFocused = true;
        usernameTextBox.caption = "Username: ";
        usernameTextBox.value = "";
          
        Button loginButton = new Button();
        loginButton.id = 3;
        loginButton.parentID = -1;
        loginButton.x = 290;
        loginButton.y = 330;
        loginButton.visibleDuring = IcePush.WELCOME;
        loginButton.width = 100;
        loginButton.height = 25;
        loginButton.actionID = 0;
        loginButton.caption = "Login";
        loginButton.bgcol = Color.gray;
        loginButton.fgcol = Color.white;

        Button helpButton = new Button();
        helpButton.id = 4;
        helpButton.parentID = -1;
        helpButton.x = 400;
        helpButton.y = 330;
        helpButton.visibleDuring = IcePush.WELCOME;
        helpButton.width = 100;
        helpButton.height = 25;
        helpButton.actionID = 1;
        helpButton.caption = "Help";
        helpButton.bgcol = Color.gray;
        helpButton.fgcol = Color.white;

        Button backButton = new Button();
        backButton.id = 5;
        backButton.parentID = -1;
        backButton.x = 350;
        backButton.y = 330;
        backButton.visibleDuring = IcePush.HELP;
        backButton.width = 100;
        backButton.height = 25;
        backButton.actionID = 2;
        backButton.caption = "Back";
        backButton.bgcol = Color.gray;
        backButton.fgcol = Color.white;

        ServerList serverList = new ServerList();
        serverList.id = 6;
        serverList.parentID = -1;
        serverList.x = 350;
        serverList.y = 170;
        serverList.visibleDuring = IcePush.WELCOME;
        serverList.actionID = 3;

        interfaces[dataComponent.id] = dataComponent;
        interfaces[serverTextBox.id] = serverTextBox;
        interfaces[usernameTextBox.id] = usernameTextBox;
        interfaces[loginButton.id] = loginButton;
        interfaces[helpButton.id] = helpButton;
        interfaces[backButton.id] = backButton;
        interfaces[serverList.id] = serverList;
		
		for(int k = 0; k < interfaces.length; k++) {
			UIComponent i = interfaces[k];
			if(i != null && i.parentID != -1) {
				if(i.parentID == k) {
					System.out.println("UIcomp " + k + " is parent of itself");
					throw new RuntimeException();
				} else {
			 		i.parent = interfaces[i.parentID];
					i.x += i.parent.x;
					i.y += i.parent.y;
				}
			}
		}
		
		actions = new Action[] { NetworkHandler.loginAction, Button.helpAction, Button.backAction, ServerList.clickedAction };
	}

	
	public static void handleClick(int x, int y) {
		for(int k = 0; k < interfaces.length; k++) {
			UIComponent c = interfaces[k];
			if(c == null || c.actionID == -1 || (c.visibleDuring & IcePush.state) == 0)
				continue;
			if(x >= c.x && x <= c.x + c.width && y >= c.y && y <= c.y + c.height) {
				actions[c.actionID].action(c, x - c.x, y - c.y);
				return;
			}
		}
	}
	
	public static void drawUI(Renderer r) {
		for(UIComponent c : interfaces) if(c != null) {
			if((c.visibleDuring & IcePush.state) != 0)
				c.draw(r);
		}
	}
	
	public void draw(Renderer r) { 
		if(parent != null) {
			parent.draw(r);
		}
		drawComponent(r);
	}
	
	protected void drawComponent(Renderer r) {
		
	}
}
