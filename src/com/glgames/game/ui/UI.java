package com.glgames.game.ui;

import java.awt.Color;

import com.glgames.game.Renderer;

public class UI extends UIComponent {
	public UIComponent dataEntryContainer;
	public UIComponent buttonContainer;
	public TextBox serverTextBox;
	public TextBox usernameTextBox;
	public Button loginButton;
	public Button helpButton;
	public Button backButton;
	public Button logoutButton;
	public ServerList serverList;

	public UI(int x, int y, int width, int height) {
		super(x, y, width, height);
		dataEntryContainer = new UIComponent(315, 275, 170, 65);
		buttonContainer = new UIComponent(290, 330, 210, 55);

		serverTextBox = new TextBox(0, 0, 170, 20);
		serverTextBox.setCaption("Server: ");
		usernameTextBox = new TextBox(0, 25, 170, 20);
		usernameTextBox.focus();
		usernameTextBox.setCaption("Username: ");

		loginButton = new Button(0, 0, 100, 25);
		loginButton.setCaption("Login");
		helpButton = new Button(110, 0, 100, 25);
		helpButton.setCaption("Help");
		backButton = new Button(55, 30, 100, 25);
		backButton.setCaption("Back");
		logoutButton = new Button(690, 2, 100, 27);
		logoutButton.setCaption("Logout");

		// The ServerList calculates its own width and height, so set to 0 for now
		serverList = new ServerList(350, 170, 0, 0);

		// Pack the components
		dataEntryContainer.addChild(serverTextBox);
		dataEntryContainer.addChild(usernameTextBox);
		buttonContainer.addChild(loginButton);
		buttonContainer.addChild(backButton);
		buttonContainer.addChild(helpButton);
		this.addChild(dataEntryContainer);
		this.addChild(buttonContainer);
		this.addChild(serverList);
		this.addChild(logoutButton);

		// Recursively make every component visible
		this.setVisibleRecursive(true);
		backButton.setVisible(false);
		logoutButton.setVisible(false);
	}
}
