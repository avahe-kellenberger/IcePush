package com.glgames.game.ui;

import java.awt.Graphics;

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
		FileBuffer cr = new FileBuffer("interfaces");
		short numInter = cr.readShort();
		interfaces = new UIComponent[numInter];
		for(short k = 0; k < numInter; k++) {
			short id = cr.readShort();
			short pID = cr.readShort();
			short x = cr.readShort();
			short y = cr.readShort();
			byte vOn = cr.readByte();
			byte type = cr.readByte();
			
			UIComponent i = null;
			if(type == 0)
				i = new UIComponent();
			else if(type == 1)
				i = new TextBox();
			else if(type == 2)
				i = new Label();
			else if(type == 3)
				i = new Button();
			else if(type == 4)
				i = new ListBox();
			else if(type == 5)
				i = new ServerList();
			
			if(i == null) {
				System.out.println("Read unknown UIcomp type " + i);
				throw new RuntimeException();
			}
			
			i.id = id;
			i.parentID = pID;
			i.x = x;
			i.y = y;
			i.visibleDuring = vOn;
			
			if(type == 1) {
				TextBox b = (TextBox) i;
				b.isFocused = cr.readByte() == 1;
				b.caption = cr.readString();
				b.value = cr.readString();
			} else if(type == 2) {
				Label l = (Label) i;
				l.caption = cr.readString();
				l.color = cr.readColor();
			} else if(type == 3) {
				Button b = (Button) i;
				b.width = cr.readShort();
				b.height = cr.readShort();
				b.actionID = cr.readByte();
				b.caption = cr.readString();
				b.bgcol = cr.readColor();
				b.fgcol = cr.readColor();
			} else if(type == 4) {
				ListBox l = (ListBox) i;
				l.actionID = cr.readByte();
				int count = cr.readByte();
				l.items = new String[count];
				for(int n = 0; n < count; n++)
					l.items[n] = cr.readString();
			} else if(type == 5) {
				ServerList s = (ServerList) i;
				s.actionID = cr.readByte();
				new Thread(s).start();
			}
			
			interfaces[id] = i;
		}
		
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
	
	public static void drawUI(Graphics g) {
		for(UIComponent c : interfaces) if(c != null) {
			if((c.visibleDuring & IcePush.state) != 0)
				c.draw(g);
		}
	}
	
	public void draw(Graphics g) { 
		if(parent != null) {
			parent.draw(g);
		}
		drawComponent(g);
	}
	
	protected void drawComponent(Graphics g) {
		
	}
}
