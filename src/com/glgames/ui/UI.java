package com.glgames.ui;

public class UI extends Container {
	public Container welcomeScreenContainer;
	public Container helpScreenContainer;
	public Container instructionsContainer;
	public Container dataEntryContainer;
	public Container buttonContainer;
	public Container helpTextContainer;
	public TextBox serverTextBox;
	public TextBox usernameTextBox;
	public Button loginButton;
	public Button helpButton;
	public Button backButton;
	public Button logoutButton;
	public Label networkStatus;

	public UI(int width, int height) {
		super(0, 0, width, height);

		String[] instructionsText = new String[] {"Push the other players off the ice!", "Try not to fall off!"};
		String[] helpText = new String[] {"Arrow keys - move", "Q - logout", "2 - 2D view", "3 - 3D view", "C - chat"};

		welcomeScreenContainer = new Container(0, 0, width, height);
		helpScreenContainer = new Container(0, 0, width, height);

		instructionsContainer = new Container(0, 140, width, 70);
		dataEntryContainer = new Container(315, 250, 170, 65);
		buttonContainer = new Container(295, 345, 210, 25);
		helpTextContainer = new Container(0, 150, width, 200);

		networkStatus = new Label("Select a username.");
		serverTextBox = new TextBox(170, 20);
		serverTextBox.setCaption("Server: ");
		usernameTextBox = new TextBox(170, 20);
		usernameTextBox.focus();
		usernameTextBox.setCaption("Username: ");

		loginButton = new Button(100, 25);
		loginButton.setCaption("Login");
		helpButton = new Button(100, 25);
		helpButton.setCaption("Help");
		backButton = new Button(350, 345, 100, 25);
		backButton.setCaption("Back");
		logoutButton = new Button(690, 2, 100, 27);
		logoutButton.setCaption("Logout");

		// Pack the components into the containers
		welcomeScreenContainer.setLayout(Container.Layout.FIXED);
		welcomeScreenContainer.addChild(instructionsContainer);
		welcomeScreenContainer.addChild(dataEntryContainer);
		welcomeScreenContainer.addChild(buttonContainer);
		instructionsContainer.setLayout(Container.Layout.VERTICAL);
		for (String line : instructionsText)
			instructionsContainer.addChild(new Label(line));
		instructionsContainer.addChild(networkStatus);
		dataEntryContainer.setLayout(Container.Layout.VERTICAL);
		dataEntryContainer.addChild(serverTextBox);
		dataEntryContainer.addChild(usernameTextBox);
		buttonContainer.addChild(loginButton);
		buttonContainer.addChild(helpButton);
		helpScreenContainer.setLayout(Container.Layout.FIXED);
		helpScreenContainer.addChild(backButton);
		helpScreenContainer.addChild(helpTextContainer);
		helpTextContainer.setLayout(Container.Layout.VERTICAL);
		for (String line : helpText)
			helpTextContainer.addChild(new Label(line));
		this.setLayout(Container.Layout.FIXED);
		this.addChild(welcomeScreenContainer);
		this.addChild(helpScreenContainer);
		this.addChild(logoutButton);

		// Recursively make every component visible
		this.setVisible(true);
		welcomeScreenContainer.setVisibleRecursive(true);
	}
}
