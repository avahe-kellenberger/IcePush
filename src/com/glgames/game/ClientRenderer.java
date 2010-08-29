package com.glgames.game;

import java.awt.*;

import com.glgames.graphics2d.*;
import com.glgames.graphics3d.*;

import com.glgames.shared.Opcodes;

public class ClientRenderer extends Renderer3D {

	public ClientRenderer(Component c, int w, int h) {
		super(c, w, h);
	}

	public void renderScene() {
		if(GRAPHICS_MODE == SOFTWARE_2D) {
			renderScene2D(GameObjects.players);
		}
		if(GRAPHICS_MODE == SOFTWARE_3D) {
			Object3D objs[] = new Object3D[GameObjects.players.length];
			for(int i = 0; i < objs.length; i++) {
				if(GameObjects.players[i] != null) {
					objs[i] = GameObjects.players[i].model;
				}
			}
			renderScene3D(objs, GameObjects.scenery);
			drawNames(GameObjects.players);
		}
		drawDeathsBox();
		if(chats_visible)
			drawNewChats();
	}

	private void drawNewChats() {
		String chat;
		
		bg.setColor(chatsBoxColor);
		bg.setFont(chatsFont);
		
		bg.fillRoundRect(50, -21, 700, 200, 50, 50);
		bg.setColor(Color.white);
		bg.drawLine(50, 155, 750, 155);
		
		for(int k = 0; k < chats.size(); k++) {
			chat = chats.get(k);
			bg.drawString(chat, 70, 160 - (chats.size() - k) * 15);
		}
		Player p = GameObjects.players[NetworkHandler.id];
		if(p == null)
			return;
		
		String str = IcePush.is_chat ? "<" + p.username + "> " + curChat + "_" : "<enter> to chat";
		bg.drawString(str, 70, 172);
	}

	private void drawNames(Player[] players) {
		for(Player p : players) if(p != null) {
			Object3D o = p.model;
			int height_variable_based_on_type = 0;
			if(p.type == Opcodes.TREE) {
				height_variable_based_on_type = 120;
			} else if(p.type == Opcodes.SNOWMAN) {
				height_variable_based_on_type = 50;
			}
			double[] pt = transformPoint(o.baseX, o.baseY, o.baseZ,
					-HALF_GAME_FIELD_WIDTH, height_variable_based_on_type, -HALF_GAME_FIELD_HEIGHT);
			int[] scr = worldToScreen(pt[0], pt[1], pt[2]);
			
			int width = bg.getFontMetrics().stringWidth(p.username) / 2;
			bg.setFont(namesFont);
			bg.setColor(Color.red);
			bg.drawString(p.username, scr[0] - width, scr[1]);
		}
	}

	protected void renderScene2D(Player[] players) {
		Graphics2D g = (Graphics2D) bg;
		if(g == null)
			return;
		g.drawImage(GameObjects.background, 0, 0, null);
		
		for (int k = 0; k < players.length; k++) {
			Player p = players[k];
			if (p == null)
				continue;
			p.draw(g);
		}
	}

	public void drawLoadingErrorIfThereIsSuchAnError() {
		if(GameObjects.error == null)
			return;
		bg.setColor(Color.red);
		bg.setFont(deathsBoxFont);
		bg.drawString(GameObjects.error, 50, 50);
	}

	private void drawDeathsBox() {
		bg.setColor(Color.white);
		bg.setFont(deathsBoxFont);
		int x = 200, y = 340;
		bg.drawString("Deaths", x, y);
		bg.drawRect(x, y += 5, 400, 100);
		for (int k = 0; k < GameObjects.players.length; k++) {
			if (GameObjects.players[k] == null) continue;
			Player plr = GameObjects.players[k];
			bg.drawString(plr.username + " - " + plr.deaths, x + 15, y += 20);
		}
	}

	public void drawHelpScreen(int cycle) {
		bg.drawImage(GameObjects.background, 0, 0, null);
		int w;
		bg.setColor(Color.white);
		bg.setFont(titleFont);
		((Graphics2D) bg).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		int y = 190;
		for (String s : GameObjects.help) {
			w = bg.getFontMetrics().stringWidth(s);
			bg.drawString(s, width / 2 - w / 2, y += 30);
		}
	}


	private void drawTopButtons() {
		int w = GameObjects.button.getWidth();
		int x = 28, y = 30;
		for(int i = 0; i < 5; i++) {
			if(i == selectedButton || i == mouseOverButton) {
				bg.drawImage(GameObjects.button2, x, y, null);
			} else {
				bg.drawImage(GameObjects.button, x, y, null);
			}
			x += w;
		}
	}

	public void drawWelcomeScreen(int cycle) {
		bg.drawImage(GameObjects.background, 0, 0, null);
		int w;
		bg.setColor(Color.white);
		bg.setFont(titleFont);

		int y = 140;
		for (String s : GameObjects.instructions) {
			w = bg.getFontMetrics().stringWidth(s);
			bg.drawString(s, width / 2 - w / 2, y += 30);
		}

		bg.setColor(Color.white);
		((Graphics2D) bg).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		w = bg.getFontMetrics().stringWidth(message);
	}

}
