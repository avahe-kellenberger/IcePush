package net.threesided.game;

import java.awt.image.BufferedImage;

import net.threesided.graphics2d.SpriteLoader;
import net.threesided.graphics3d.Object3D;
import net.threesided.ui.UIComponent;
import net.threesided.ui.MapCanvas;
import net.threesided.ui.TextBox;
import net.threesided.ui.Action;
import net.threesided.ui.UI;

public class GameObjects {
	public static UI ui;

//	public static boolean loaded = false;
	
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
	

	public static Action onUsernameTextBoxClick = new Action() {
		public void doAction(UIComponent uiComp, int x, int y) {
			ui.serverTextBox.unfocus();
			ui.usernameTextBox.focus();
		}
	};

	public static Action onServerTextBoxClick = new Action() {
		public void doAction(UIComponent uiComp, int x, int y) {
			ui.usernameTextBox.unfocus();
			ui.serverTextBox.focus();
		}
	};

	public static Action onSelectButtonClick = new Action() {
		public void doAction(UIComponent uiComp, int x, int y) {
			ui.mapCanvas.setTool(MapCanvas.Tool.SELECT);
		}
	};

	public static Action onLineButtonClick = new Action() {
		public void doAction(UIComponent uiComp, int x, int y) {
			ui.mapCanvas.setTool(MapCanvas.Tool.LINE);
		}
	};

	public static Action onQuadButtonClick = new Action() {
		public void doAction(UIComponent uiComp, int x, int y) {
			ui.mapCanvas.setTool(MapCanvas.Tool.QUADRATIC);
		}
	};

	public static Action onCubicButtonClick = new Action() {
		public void doAction(UIComponent uiComp, int x, int y) {
			ui.mapCanvas.setTool(MapCanvas.Tool.CUBIC);
		}
	};

	public static Action onCloseButtonClick = new Action() {
		public void doAction(UIComponent uiComp, int x, int y) {
			ui.mapCanvas.closePath();
		}
	};

	public static Action onExportButtonClick = new Action() {
		public void doAction(UIComponent uiComp, int x, int y) {
			ui.mapCanvas.exportPath();
		}
	};

	public static Action onImportButtonClick = new Action() {
		public void doAction(UIComponent uiComp, int x, int y) {
			ui.mapCanvas.importPath();
		}
	};


	public static void load(int width, int height) {

		try {
			ui = new UI(width, height);
			if(serverMode == LIST_FROM_SERVER) {
				ui.serverTextBox.setVisible(false);
			} else if(serverMode == TYPE_IN_BOX) {
				//ui.serverList.setVisible(false);
			} else {
				ui.serverTextBox.setVisible(false);
				//ui.serverList.setVisible(false);
			}
			
			ui.serverTextBox.setClickAction(onServerTextBoxClick);
			ui.usernameTextBox.setClickAction(onUsernameTextBoxClick);
			ui.selectButton.setClickAction(onSelectButtonClick);
			ui.lineButton.setClickAction(onLineButtonClick);
			ui.quadButton.setClickAction(onQuadButtonClick);
			ui.cubicButton.setClickAction(onCubicButtonClick);
			ui.closeButton.setClickAction(onCloseButtonClick);
			ui.exportButton.setClickAction(onExportButtonClick);
			ui.importButton.setClickAction(onImportButtonClick);

			logo = SpriteLoader.getSprite("images/logo.png");
			button = SpriteLoader.getSprite("images/button.png");
			button2 = SpriteLoader.getSprite("images/button2.png");		
			dbox = SpriteLoader.getSprite("images/dbox.png");
			
			scenery = new Object3D[10]; 
			scenery[0] = new Object3D.Plane();//(0, 0, 0, 20, 12, 40);
			
			background = SpriteLoader.getSprite("images/icepush.png");
	//		loaded = true;
		} catch(Exception e) {
			error = e.toString();
			e.printStackTrace();
		}
	}
}

