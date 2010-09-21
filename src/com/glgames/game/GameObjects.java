package com.glgames.game;

import java.awt.image.BufferedImage;

import com.glgames.ui.*;
import com.glgames.graphics2d.SpriteLoader;
import com.glgames.graphics3d.ObjImporter;
import com.glgames.graphics3d.Object3D;

public class GameObjects {
	public static UI ui;

	public static boolean loaded = false;
	
	public static String[] instructions, help;
	public static BufferedImage logo;
	public static BufferedImage background;
	public static BufferedImage dbox;

	public static BufferedImage button, button2;
	public static TextBox serverBox, usernameBox;
	//public static ServerList serverList;

	public static Player[] players;
	public static Object3D[] scenery; // Right now scenery is only used in 3D mode
	public static String error;
	
	// World list stuff
	public static final int TYPE_IN_BOX = 0;
	public static final int LIST_FROM_SERVER = 1;
	public static final int USE_DEFAULT = 3;
	public static int serverMode = TYPE_IN_BOX;
	
	public static void load() {

		try {
			ui = new UI(IcePush.WIDTH, IcePush.HEIGHT);
			if(serverMode == LIST_FROM_SERVER) {
				ui.serverTextBox.setVisible(false);
			} else if(serverMode == TYPE_IN_BOX) {
				//ui.serverList.setVisible(false);
			} else {
				ui.serverTextBox.setVisible(false);
				//ui.serverList.setVisible(false);
			}
			
			ui.serverTextBox.setText(NetworkHandler.DEFAULT_SERVER);
			if(IcePush.username != null) ui.usernameTextBox.setText(IcePush.username);
			ui.serverTextBox.setClickAction(IcePush.onServerTextBoxClick);
			ui.usernameTextBox.setClickAction(IcePush.onUsernameTextBoxClick);
			//ui.serverList.setClickAction(ServerList.onServerListClick);
			ui.loginButton.setClickAction(NetworkHandler.onLoginButtonClick);
			ui.logoutButton.setClickAction(NetworkHandler.onLogoutButtonClick);
			ui.helpButton.setClickAction(IcePush.onHelpButtonClick);
			ui.mapEditorButton.setClickAction(IcePush.onMapEditorButtonClick);
			ui.backButton.setClickAction(IcePush.onBackButtonClick);
			ui.selectButton.setClickAction(IcePush.onSelectButtonClick);
			ui.lineButton.setClickAction(IcePush.onLineButtonClick);
			ui.quadButton.setClickAction(IcePush.onQuadButtonClick);
			ui.cubicButton.setClickAction(IcePush.onCubicButtonClick);
			ui.closeButton.setClickAction(IcePush.onCloseButtonClick);
			ui.exportButton.setClickAction(IcePush.onExportButtonClick);
			ui.importButton.setClickAction(IcePush.onImportButtonClick);

			logo = SpriteLoader.getSprite("images/logo.png");
			button = SpriteLoader.getSprite("images/button.png");
			button2 = SpriteLoader.getSprite("images/button2.png");		
			dbox = SpriteLoader.getSprite("images/dbox.png");
			
			scenery = new Object3D[10]; 
			scenery[0] = new Object3D.Plane(0, 0, 0, 20, 12, 40);
			
			background = SpriteLoader.getSprite("images/icepush.png");
			loaded = true;
		} catch(Exception e) {
			error = e.toString();
			e.printStackTrace();
		}
	}
}
