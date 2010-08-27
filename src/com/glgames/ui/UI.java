package com.glgames.ui;

import com.glgames.graphics2d.Renderer;

public class UI extends Container {
	public Container dataEntryContainer;
	public Container buttonContainer;
	public TextBox serverTextBox;
	public TextBox usernameTextBox;
	public Button loginButton;
	public Button helpButton;
	public Button backButton;
	public Button logoutButton;
	//public ServerList serverList;

	public UI(int width, int height) {
		super(0, 0, width, height);
		dataEntryContainer = new Container(315, 250, 170, 65);
		buttonContainer = new Container(295, 345, 210, 25);

		serverTextBox = new TextBox(170, 20);
		serverTextBox.setCaption("Server: ");
		usernameTextBox = new TextBox(170, 20);
		usernameTextBox.focus();
		usernameTextBox.setCaption("Username: ");

		loginButton = new Button(100, 25);
		loginButton.setCaption("Login");
		helpButton = new Button(100, 25);
		helpButton.setCaption("Help");
		backButton = new Button(100, 25);
		backButton.setCaption("Back");
		logoutButton = new Button(690, 2, 100, 27);
		logoutButton.setCaption("Logout");

		// The ServerList calculates its own width and height, so set to 0 for now
		//serverList = new ServerList(350, 170);

		// Pack the components
		dataEntryContainer.setLayout(Container.Layout.VERTICAL);
		dataEntryContainer.addChild(serverTextBox);
		dataEntryContainer.addChild(usernameTextBox);
		buttonContainer.addChild(loginButton);
		buttonContainer.addChild(backButton);
		buttonContainer.addChild(helpButton);
		this.setLayout(Container.Layout.FIXED);
		this.addChild(dataEntryContainer);
		this.addChild(buttonContainer);
		//this.addChild(serverList);
		this.addChild(logoutButton);

		// Recursively make every component visible
		this.setVisibleRecursive(true);
		backButton.setVisible(false);
		logoutButton.setVisible(false);
	}
}
