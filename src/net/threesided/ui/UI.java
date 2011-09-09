package net.threesided.ui;

public class UI extends Container {
	public Container welcomeScreenContainer;
	public Container helpScreenContainer;
	public Container mapEditorScreenContainer;

	public Container instructionsContainer;
	public Container dataEntryContainer;
	public Container buttonContainer;
	public Container buttonInnerContainer;
	public Container helpTextContainer;
	public Container toolContainer;

	public Label networkStatus;
	public TextBox serverTextBox;
	public TextBox usernameTextBox;
	public Button loginButton;
	public Button helpButton;
	public Button mapEditorButton;
	public Button backButton;
	public Button logoutButton;
	public MapCanvas mapCanvas;
	public Button selectButton;
	public Button lineButton;
	public Button quadButton;
	public Button cubicButton;
	public Button closeButton;
	public Button exportButton;
	public Button importButton;

	public UI(int width, int height) {
		super(0, 0, width, height);

		String[] instructionsText = new String[] {"Push the other players off the ice!", "Try not to fall off!"};
		String[] helpText = new String[] {"Arrow keys - move", "PgUp/PgDn - zoom camera", "Q - logout", "2 - 2D view", "3 - 3D view", "C - chat"};

		// Create screen containers
		welcomeScreenContainer = new Container(0, 0, width, height);
		helpScreenContainer = new Container(0, 0, width, height);
		mapEditorScreenContainer = new Container(0, 0, width, height);

		// Create sub-containers
		instructionsContainer = new Container(0, 140, width, 70);
		dataEntryContainer = new Container(315, 250, 170, 65);
		buttonContainer = new Container(295, 330, 210, 60);
		buttonInnerContainer = new Container(210, 25);
		helpTextContainer = new Container(0, 150, width, 200);
		toolContainer = new Container(10, 450, 780, 25);

		// Create welcome screen components
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
		mapEditorButton = new Button(150, 25);
		mapEditorButton.setCaption("Map Editor");

		// Create help screen components
		backButton = new Button(350, 345, 100, 25);
		backButton.setCaption("Back");
		logoutButton = new Button(690, 2, 100, 27);
		logoutButton.setCaption("Logout");

		// Create map screen components
		mapCanvas = new MapCanvas(0, 0, width, height - 40);
		selectButton = new Button(100, 25);
		selectButton.setCaption("Select");
		lineButton = new Button(100, 25);
		lineButton.setCaption("Line");
		quadButton = new Button(100, 25);
		quadButton.setCaption("Quad");
		cubicButton = new Button(100, 25);
		cubicButton.setCaption("Cubic");
		closeButton = new Button(100, 25);
		closeButton.setCaption("Close Path");
		exportButton = new Button(100, 25);
		exportButton.setCaption("Export");
		importButton = new Button(100, 25);
		importButton.setCaption("Import");

		// Pack the welcome screen
		instructionsContainer.setLayout(Container.Layout.VERTICAL);
		for (String line : instructionsText)
			instructionsContainer.addChild(new Label(line));
		instructionsContainer.addChild(networkStatus);
		dataEntryContainer.setLayout(Container.Layout.VERTICAL);
		dataEntryContainer.addChild(serverTextBox);
		dataEntryContainer.addChild(usernameTextBox);
		buttonContainer.setLayout(Container.Layout.VERTICAL);
		buttonInnerContainer.addChild(loginButton);
		buttonInnerContainer.addChild(helpButton);
		buttonContainer.addChild(buttonInnerContainer);
		buttonContainer.addChild(mapEditorButton);
		welcomeScreenContainer.setLayout(Container.Layout.FIXED);
		welcomeScreenContainer.addChild(instructionsContainer);
		welcomeScreenContainer.addChild(dataEntryContainer);
		welcomeScreenContainer.addChild(buttonContainer);

		// Pack the help screen
		helpTextContainer.setLayout(Container.Layout.VERTICAL);
		for (String line : helpText)
			helpTextContainer.addChild(new Label(line));
		helpScreenContainer.setLayout(Container.Layout.FIXED);
		helpScreenContainer.addChild(backButton);
		helpScreenContainer.addChild(helpTextContainer);

		// Pack the map screen
		toolContainer.addChild(selectButton);
		toolContainer.addChild(lineButton);
		toolContainer.addChild(quadButton);
		toolContainer.addChild(cubicButton);
		toolContainer.addChild(closeButton);
		toolContainer.addChild(exportButton);
		toolContainer.addChild(importButton);
		mapEditorScreenContainer.setLayout(Container.Layout.FIXED);
		mapEditorScreenContainer.addChild(mapCanvas);
		mapEditorScreenContainer.addChild(toolContainer);

		// Pack the root UI
		this.setLayout(Container.Layout.FIXED);
		this.addChild(welcomeScreenContainer);
		this.addChild(helpScreenContainer);
		this.addChild(mapEditorScreenContainer);
		this.addChild(logoutButton);

		// Show nothing except the welcome screen
		this.setVisible(true);
		welcomeScreenContainer.setVisibleRecursive(true);
	}
}
